package nl.siegmann.epublib.domain;

public class Meta {

	private String name;
	private String content;
	
	public Meta(String name, String content) {
		this.name = name;
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String getPrefix() {
		if(getName().indexOf(':') != -1) {
			return getName().substring(0, getName().indexOf(':'));
		}
		return "";
	}
	
	public String getLocalPart() {
		if(getName().indexOf(':') != -1) {
			return getName().substring(getName().indexOf(':')+1);
		}
		return "";
	}	
	
	public String toString() {
		return name + "=" + content;
	}
	
}
