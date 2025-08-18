package com.roth.servlet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For use on a bean object's setter for a byte[] field to indicate that the source is a multipart form file input.  
 * The fieldname attribute indicates which field in the bean is intended to store the file's filename.  The mimetype
 * attribute indicates which field in teh bean is intended to store the file's mime type; this is detected using the
 * Tika library, and is not defined by the file extension.  Note that ActionServlet's POST processing will look for 
 * the setter for the field, rather than the field itself.  The allowed attribute defines what mime types are allowed;
 * use '*' as a wild card (the default value is "*").  
 * @author jpayne
 * @see {@link @SimpleAction}
 * @see {@link @Forward}
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Multipart {
	String filename();
	String mimeType();
	String[] allowed() default {"*"};
}
