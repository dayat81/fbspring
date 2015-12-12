package dayat.fbspring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {

	@Id
	private String id;

	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getRef() {
		return ref;
	}
	String name;
	String ref;
//getter, setter, toString, Constructors

}
