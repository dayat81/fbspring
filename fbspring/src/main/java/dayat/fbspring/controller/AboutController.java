package dayat.fbspring.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import dayat.fbspring.config.SpringMongoConfig;
import facebook4j.Facebook;
import facebook4j.RawAPIResponse;
import facebook4j.auth.AccessToken;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONObject;

@Controller
public class AboutController {

	@RequestMapping(value="/callback")
	public void callback(HttpServletRequest request,HttpServletResponse response) throws IOException{
		Facebook facebook = (Facebook) request.getSession().getAttribute("fb");
        String oauthCode = request.getParameter("code");
        try {
        	AccessToken info = facebook.getOAuthAccessToken(oauthCode);
        	System.out.println(info.getToken());
        	System.out.println(facebook.getId());
        	System.out.println(facebook.getName());
        	ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
    	    MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
    	    DBCollection coll = mongoOperation.getCollection("users");
        	//search user first
    	    BasicDBObject whereQuery = new BasicDBObject();
    	    whereQuery.put("id", facebook.getId());
    	    BasicDBObject fields = new BasicDBObject("_id",false);
    	    DBCursor cursor = coll.find(whereQuery,fields);							
			if(cursor.hasNext()) {
				//save user object to session
				DBObject myfb = cursor.next();
				request.getSession().setAttribute("myfb", myfb);
				//update token if user exist
				DBObject listItem = new BasicDBObject("token", info.getToken());
				DBObject updateQuery = new BasicDBObject("$set", listItem);
				coll.update(whereQuery, updateQuery);				
			}else{    
				//insert new user
	    	    DBObject listItem = new BasicDBObject("id", facebook.getId()).append("name", facebook.getName()).append("token", info.getToken());
	    	    coll.insert(listItem);
			}
        	request.getSession().setAttribute("facebook", facebook);
        }catch (Exception e) {
            e.printStackTrace();
        }
        StringBuffer callbackURL = request.getRequestURL();
        int index = callbackURL.lastIndexOf("/");
        callbackURL.replace(index, callbackURL.length(), "").append("/about");
		response.sendRedirect(callbackURL.toString());
	}
	
	//update member by topic
	@RequestMapping(value="/feed")
	public void update(HttpServletRequest request,HttpServletResponse response) throws IOException{
		String id = request.getParameter("id");
		Facebook facebook = (Facebook) request.getSession().getAttribute("facebook");
		if(facebook!=null){
			try{
			    RawAPIResponse res = facebook.callGetAPI(id+"/feed");
			    JSONObject jsonObject = res.asJSONObject();
			    JSONArray data = (JSONArray)jsonObject.get("data");
			    for (int i = 0, size = data.length(); i < size; i++)
			    {
			      JSONObject objectInArray = data.getJSONObject(i);
			      System.out.println(objectInArray);
			    }	
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			
		}
	}
	@RequestMapping(value="/about")
	public void about(HttpServletRequest request,HttpServletResponse response) throws IOException{
		Facebook facebook = (Facebook) request.getSession().getAttribute("facebook");
		if(facebook!=null){
			//get team member feed
			try{
				//get myfb
				DBObject myfb = (DBObject)request.getSession().getAttribute("myfb");
		      	ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
	    	    MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
	    	    DBCollection coll = mongoOperation.getCollection("users");
	    	    BasicDBObject whereQuery = new BasicDBObject();
	    	    whereQuery.put("ref", facebook.getId());
	    	    BasicDBObject fields = new BasicDBObject("_id",false);
	    	    DBCursor cursor = coll.find(whereQuery,fields);	
	    	    //JSONArray list = new JSONArray();
	    	    DBObject[] list = new DBObject[cursor.size()];
	    	    int i=0;
				while(cursor.hasNext()) {
					//team member
					DBObject fb = cursor.next();	
					System.out.println(fb);
					list[i]=fb;
					i++;
				}
				DBObject myjsondata = new BasicDBObject("mydata", myfb).append("team", list);
				response.getWriter().append(myjsondata.toString());
			}catch(Exception e){
				e.printStackTrace();
			}
//	        try {
//	        	ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
//	    	    MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
//	    	    Query searchUserQuery = new Query(Criteria.where("ref").is(facebook.getId()));
//	    	    List<User> savedUser = mongoOperation.find(searchUserQuery, User.class);
//	    	    request.getSession().setAttribute("users", savedUser);
//	    	    RawAPIResponse res = facebook.callGetAPI("me/feed");
//	    	    JSONObject jsonObject = res.asJSONObject();
//	    	    JSONArray data = (JSONArray)jsonObject.get("data");
//	    	    for (int i = 0, size = data.length(); i < size; i++)
//	    	    {
//	    	      JSONObject objectInArray = data.getJSONObject(i);
//	    	      System.out.println(objectInArray);
//	    	    }
//	    	    JSONObject paging = (JSONObject) jsonObject.get("paging");
//	    	    String url=paging.get("next").toString();
//	    	    System.out.println(url);
//	    		URL obj = new URL(url);
//	    		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//
//	    		// optional default is GET
//	    		con.setRequestMethod("GET");
//
//	    		//add request header
//	    		con.setRequestProperty("User-Agent", "Mozilla/5.0");
//
//	    		int responseCode = con.getResponseCode();
//	    		System.out.println("\nSending 'GET' request to URL : " + url);
//	    		System.out.println("Response Code : " + responseCode);
//	    		BufferedReader in = new BufferedReader(
//	    		        new InputStreamReader(con.getInputStream()));
//	    		String inputLine;
//	    		StringBuffer resp = new StringBuffer();
//
//	    		while ((inputLine = in.readLine()) != null) {
//	    			resp.append(inputLine);
//	    		}
//	    		in.close();
//
//	    		//print result
//	    		System.out.println(resp.toString());
//	    		JSONObject page1 = new JSONObject(resp.toString());
//	    		JSONArray data1 = (JSONArray)page1.get("data");
//	    	    for (int i = 0, size = data1.length(); i < size; i++)
//	    	    {
//	    	      JSONObject objectInArray = data1.getJSONObject(i);
//	    	      System.out.println(objectInArray);
//	    	    }
//	    		return new ModelAndView("about");
//	        }catch (Exception e) {
//	            e.printStackTrace();
//	        }			
		}else{
	        StringBuffer callbackURL = request.getRequestURL();
	        int index = callbackURL.lastIndexOf("/");
	        callbackURL.replace(index, callbackURL.length(), "").append("/");
			response.sendRedirect(callbackURL.toString());
			response.getWriter().append("plese relogin");
		}
	}
}
