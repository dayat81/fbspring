package dayat.fbspring.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import dayat.fbspring.config.SpringMongoConfig;
import facebook4j.Facebook;
import facebook4j.FacebookFactory;

@Controller
public class HomeController {

	@RequestMapping(value="/")
	public void home(HttpServletRequest request,HttpServletResponse response) throws IOException{
		Facebook facebook = (Facebook) request.getSession().getAttribute("facebook");
		if(facebook==null){
	        facebook = new FacebookFactory().getInstance();
	        request.getSession().setAttribute("fb", facebook);
	        String ref=request.getParameter("ref");
	        request.getSession().setAttribute("ref", ref);
	        StringBuffer callbackURL = request.getRequestURL();
	        int index = callbackURL.lastIndexOf("/");
	        callbackURL.replace(index, callbackURL.length(), "").append("/callback");
	        response.sendRedirect(facebook.getOAuthAuthorizationURL(callbackURL.toString()));
		}else{
			//call /about
	        StringBuffer callbackURL = request.getRequestURL();
	        int index = callbackURL.lastIndexOf("/");
	        callbackURL.replace(index, callbackURL.length(), "").append("/about");
			response.sendRedirect(callbackURL.toString());
		}
		//return new ModelAndView("home");
	}
	
	@RequestMapping(value="/test")
	public void test(HttpServletRequest request,HttpServletResponse response){
		try {
			ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
		    MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
			DBCollection coll = mongoOperation.getCollection("users");
			DBCursor cursor1 = coll.find();
			while(cursor1.hasNext()){
				System.out.println(cursor1.next());
			}
			response.getWriter().append("Root Served at: ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
