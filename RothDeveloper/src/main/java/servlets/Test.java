package servlets;

import com.roth.base.util.Data;
import com.roth.base.util.Data.Pad;
import com.roth.servlet.ActionServlet;

import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.WebServlet;

/**
 * Servlet implementation class Test
 */
@WebServlet("/Test/*")
public class Test extends ActionServlet implements Servlet {
	private static final long serialVersionUID = 1L;

	@Action(responses = { @Response(name = SUCCESS) })
	public String begin(ActionConnection conn) throws Exception {
		/*
		ShellOutput output = Shell.exec("notepad.exe");
		
		conn.println("Exit Code: " + output.getExitCode());
		conn.println("\nStandard Out: " + output.getStdOut());
		conn.println("\nError Out: " + output.getStdErr());
		*/
		
		String x = "27668cf5-b7f3-409f-a830-f864d4ad6a70";
		String c1 = "(&4sV'3((!$)Q\"%@=\"`8V@U~QHV#BQ#Ta&Uf'%g'~hQ&4TV~'{~'$^HS,D$T2{~Z&{#g(~W\"7FR.R{$(";
		String c2 = "_&)'V'H((Z$)^a%5T\"Y#VcX~QTV#WQ#&G&_h'Te'~XQ&[TVR|{!^$CISYe$TO{~Y&{9W(jC\"9CR1D{$1";
		String c3 = "l&6iV'!((%$)_^%EG\">!V>`~Q~V#~Q#Ty&3e'tS'~aQ&YTVgN{~C$h(Sdt$TW{~\"&{%((\"D\"?aR7={$a";
		
		conn.println("x: " + x);
		conn.println("x.hashCode: " + x.hashCode());
		conn.println("c1: " + c1);
		conn.println("Decode of c1:\n   " + Data.decrypt(c1, x.hashCode()));
		conn.println("x: " + x);
		conn.println("c2: " + c2);
		conn.println("Decode of c2:\n   " + Data.decrypt(c2, x.hashCode()));
		conn.println("x: " + x);
		conn.println("c2: " + c3);
		conn.println("Decode of c3:\n   " + Data.decrypt(c3, x.hashCode()));
		conn.println("x: " + x);
		return SUCCESS;
	}

	@Action(responses = { @Response(name = SUCCESS) })
	public String testAll(ActionConnection conn) throws Exception {
		conn.println("All Works");
		return SUCCESS;
	}
	
	@Action(responses = { @Response(name = SUCCESS) })
	@MethodSecurity(methods = GET)
	public String testGet(ActionConnection conn) throws Exception {
		conn.println("GET Works");		
		return SUCCESS;
	}
	
	@Action(responses = { @Response(name = SUCCESS) })
	@MethodSecurity(methods = { GET, POST })
	public String testGetPost(ActionConnection conn) throws Exception {
		conn.println("GET/POST Works");		
		return SUCCESS;
	}
	
	@Action(responses = { @Response(name = SUCCESS) })
	@MethodSecurity(methods = { POST })
	public String testPost(ActionConnection conn) throws Exception {
		conn.println("POST Works");		
		return SUCCESS;
	}
	
	@Action(responses = { @Response(name = SUCCESS) })
	@MethodSecurity(methods = { DELETE })
	public String testDelete(ActionConnection conn) throws Exception {
		conn.println("DELETE Works");		
		return SUCCESS;
	}
	
	@Action(responses = { @Response(name = SUCCESS) })
	@MethodSecurity(methods = { PUT })
	public String testPut(ActionConnection conn) throws Exception {
		conn.println("PUT Works");		
		return SUCCESS;
	}
	
	@Action(responses = { @Response(name = SUCCESS) })
	@MethodSecurity(roles = "Authenticated", methods = GET)
	public String testGetRole(ActionConnection conn) throws Exception {
		conn.println("GET With Role Works");		
		return SUCCESS;
	}
	
	
	@Action(responses = { @Response(name = SUCCESS) })
	public String testHex(ActionConnection conn) throws Exception {
		int red = conn.getInteger("red", 0);
		int green = conn.getInteger("green", 0);
		int blue = conn.getInteger("blue", 0);
		double opacity = conn.getDouble("opacity", 1.0);
		
		int adjOpacity = (int)Math.round(255 * opacity);
		boolean isSch = sch(red) && sch(green) && sch(blue) && sch(adjOpacity);
		String result = "#" + hex(red, isSch) + hex(green, isSch) + hex(blue, isSch);
		if (opacity < 1.0)
			result += hex(adjOpacity, isSch);
		conn.println(result);
		
		return SUCCESS;
	}
	
	private boolean sch(int value) {
		return value % 17 == 0;
	}
	private String hex(int value, boolean single) {
		String h = Integer.toHexString(single ? value / 17 : value);
		return single ? h : Data.pad(h, '0', 2, Pad.LEFT);
	}
	
	@Action(responses = { @Response(name = SUCCESS) })
	public String testDigest(ActionConnection conn) throws Exception {
		String source = "Password";
		conn.println(hash(source, "SHA-256"));
		conn.println(hash(source, "SHA-512"));
		conn.println(hash(source, "SHA3-512"));
		return SUCCESS;
	}
	
	private String hash(String source, String algorithm) {
		long start = System.nanoTime();
		String digested = Data.digest(source, algorithm);
		if ("SHA3-512".equals(algorithm))
			digested += Data.digest("" + source.hashCode(), "SHA3-512");
		long runtime = System.nanoTime() - start;
		return String.format("%s: %s %d nanoseconds", algorithm, digested, runtime);
	}

	@Action(responses = { @Response(name = SUCCESS) })
	public String testColor(ActionConnection conn) throws Exception {
		String item = """
        <div class="test" style="background: $1;"></div>
        """;
/*
		String palettes = "BERRY<div class=\"break\"></div>";
		for (Color color : Palette.getBerry())
			palettes += item.replace("$1", color.toHexa());
		palettes += "<div class=\"break\"></div>" +
					"BRIGHT<div class=\"break\"></div>";
		for (Color color : Palette.getBright())
			palettes += item.replace("$1", color.toHexa());
		palettes += "<div class=\"break\"></div>" +
					"BRIGHT_PASTEL<div class=\"break\"></div>";
		for (Color color : Palette.getBrightPastel())
			palettes += item.replace("$1", color.toHexa());
		palettes += "<div class=\"break\"></div>" +
					"CHOCOLATE<div class=\"break\"></div>";
		for (Color color : Palette.getChocolate())
			palettes += item.replace("$1", color.toHexa());
		palettes += "<div class=\"break\"></div>" +
					"EARTH<div class=\"break\"></div>";
		for (Color color : Palette.getEarth())
			palettes += item.replace("$1", color.toHexa());
		palettes += "<div class=\"break\"></div>" +
				 	"EXCEL<div class=\"break\"></div>";
		for (Color color : Palette.getExcel())
			palettes += item.replace("$1", color.toHexa());
		palettes += "<div class=\"break\"></div>" +
					"FIRE<div class=\"break\"></div>";
		for (Color color : Palette.getFire())
			palettes += item.replace("$1", color.toHexa());
		palettes += "<div class=\"break\"></div>" +
			 	 	"GRAY_SCALE<div class=\"break\"></div>";
		for (Color color : Palette.getGrayScale())
			palettes += item.replace("$1", color.toHexa());
		palettes += "<div class=\"break\"></div>" +
		 	 	 	"LIGHT<div class=\"break\"></div>";
		for (Color color : Palette.getLight())
			palettes += item.replace("$1", color.toHexa());
		palettes += "<div class=\"break\"></div>" +
	 	 	 	 	"PASTEL<div class=\"break\"></div>";
		for (Color color : Palette.getPastel())
			palettes += item.replace("$1", color.toHexa());
		palettes += "<div class=\"break\"></div>" +
 	 	 	 		"SEA_GREEN<div class=\"break\"></div>";
		for (Color color : Palette.getSeaGreen())
			palettes += item.replace("$1", color.toHexa());
		palettes += "<div class=\"break\"></div>" +
	 	 	 		"SEMI_TRANSPARENT<div class=\"break\"></div>";
		for (Color color : Palette.getSemiTransparent())
			palettes += item.replace("$1", color.toHexa());
		palettes += "<div class=\"break\"></div>" +
 	 	 		"ALT_GRAYSCALE<div class=\"break\"></div>";
		for (int i = 0; i < 16; i++)
			palettes += item.replace("$1", "#" + Integer.toHexString(i) + Integer.toHexString(i) + Integer.toHexString(i));
	
		String page = """
                <!DOCTYPE html>
                <html>
                    <head>
                        <style type = "text/css">
                            .test {
                                float: left; 
                                width: 20px; 
                                height: 20px;
                                border: 2px solid #444;
                                margin-right: 6px;
                            }
                            .break {
                                clear: both;
                                height: 6px;
                            }
                        </style>
                    </head>
                    <body>
                        $1
                    </body>
                </html>
                """
				.replace("$1", palettes);
		
		conn.getResponse().setContentType("text/html");
		conn.print(page);
		*/
		return SUCCESS;
	}
	
	@Action(responses = @Response(name = SUCCESS))
	public String testSubPath(ActionConnection conn) throws Exception {
		conn.println("String(0): " + conn.getSubPath().getString(0));
		conn.println("Long(1): " + conn.getSubPath().getLong(1));
		conn.println("LocalDateTime(2): " + conn.getSubPath().getLocalDateTime(1));
		return SUCCESS;
	}
}
