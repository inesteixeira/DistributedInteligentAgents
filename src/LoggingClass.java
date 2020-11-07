import java.util.logging.Logger;
import java.util.logging.Level;

public class LoggingClass {
	
	public static void main(String[] args) {
		Logger logger = Logger.getLogger(LoggingClass.class.getName());
		logger.setLevel(Level.INFO);
	}
	
}
