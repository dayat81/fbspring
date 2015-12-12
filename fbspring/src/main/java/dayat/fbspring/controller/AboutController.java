package dayat.fbspring.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import dayat.fbspring.config.SpringMongoConfig;
import dayat.fbspring.model.User;
import facebook4j.Facebook;
import facebook4j.RawAPIResponse;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONObject;

@Controller
public class AboutController {

	@RequestMapping(value="/callback")
	public void callback(HttpServletRequest request,HttpServletResponse response) throws IOException{
		Facebook facebook = (Facebook) request.getSession().getAttribute("fb");
        String oauthCode = request.getParameter("code");
        try {
        	facebook.getOAuthAccessToken(oauthCode);
        	request.getSession().setAttribute("facebook", facebook);
        }catch (Exception e) {
            e.printStackTrace();
        }
        StringBuffer callbackURL = request.getRequestURL();
        int index = callbackURL.lastIndexOf("/");
        callbackURL.replace(index, callbackURL.length(), "").append("/about");
		response.sendRedirect(callbackURL.toString());
	}
	
	@RequestMapping(value="/about")
	public ModelAndView about(HttpServletRequest request,HttpServletResponse response) throws IOException{
		Facebook facebook = (Facebook) request.getSession().getAttribute("facebook");
		if(facebook!=null){
	        try {
	        	ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
	    	    MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
	    	    Query searchUserQuery = new Query(Criteria.where("ref").is(facebook.getId()));
	    	    List<User> savedUser = mongoOperation.find(searchUserQuery, User.class);
	    	    request.getSession().setAttribute("users", savedUser);
	    	    RawAPIResponse res = facebook.callGetAPI("me/feed");
	    	    JSONObject jsonObject = res.asJSONObject();
	    	    JSONArray data = (JSONArray)jsonObject.get("data");
	    	    for (int i = 0, size = data.length(); i < size; i++)
	    	    {
	    	      JSONObject objectInArray = data.getJSONObject(i);
	    	      System.out.println(objectInArray);
	    	    }
	    	    JSONObject paging = (JSONObject) jsonObject.get("paging");
	    	    String url=paging.get("next").toString();
	    	    System.out.println(url);
	    		URL obj = new URL(url);
	    		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

	    		// optional default is GET
	    		con.setRequestMethod("GET");

	    		//add request header
	    		con.setRequestProperty("User-Agent", "Mozilla/5.0");

	    		int responseCode = con.getResponseCode();
	    		System.out.println("\nSending 'GET' request to URL : " + url);
	    		System.out.println("Response Code : " + responseCode);
	    		BufferedReader in = new BufferedReader(
	    		        new InputStreamReader(con.getInputStream()));
	    		String inputLine;
	    		StringBuffer resp = new StringBuffer();

	    		while ((inputLine = in.readLine()) != null) {
	    			resp.append(inputLine);
	    		}
	    		in.close();

	    		//print result
	    		System.out.println(resp.toString());
	    		JSONObject page1 = new JSONObject(resp.toString());
	    		JSONArray data1 = (JSONArray)page1.get("data");
	    	    for (int i = 0, size = data1.length(); i < size; i++)
	    	    {
	    	      JSONObject objectInArray = data1.getJSONObject(i);
	    	      System.out.println(objectInArray);
	    	    }
	    		return new ModelAndView("about");
	        }catch (Exception e) {
	            e.printStackTrace();
	        }
	        return new ModelAndView("home");
		}else{
	        StringBuffer callbackURL = request.getRequestURL();
	        int index = callbackURL.lastIndexOf("/");
	        callbackURL.replace(index, callbackURL.length(), "").append("/");
			response.sendRedirect(callbackURL.toString());
			return new ModelAndView("home");
		}
	}
}
