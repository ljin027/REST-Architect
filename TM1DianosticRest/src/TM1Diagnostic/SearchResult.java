package TM1Diagnostic;

import java.util.Objects;

public class SearchResult {
	
	public Object tm1object;
	//public int lineNumber;
	public String details;
	
	public SearchResult(Object tm1object, String details){
		this.tm1object = tm1object;
		//this.lineNumber = lineNumber;
		this.details = details;
	}
	

	
	@Override
	public boolean equals(Object o) {

		if (o == this)
			return true;
		if (!(o instanceof SearchResult)) {
			return false;
		}
		SearchResult r = (SearchResult) o;
		return r.hashCode() == this.hashCode();
	}

	@Override
	public int hashCode() {
		return Objects.hash(tm1object + details);
	}
}
