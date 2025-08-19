Roth is a Web application development framework based on Java and Tomcat.  While the projects are configured for Eclipse in Windows, they are also configured for Maven build, so they should import fine in other IDEs.

NOTE: The following instructions are only needed for building the Roth framework.  If you just want to use the framework to develop other applicaitons, then download the deployment package and follow the instructions within it.

Explanation of Projects
-----------------------

There are four projects in the framework:
- **roth-lib** - The library (JAR) that provides the backend funcitonality of the framework.
- **Roth** - The application (WAR) that provides the front-end functionality of the framework and the configuration screens.  Note that you can use the library on its own with other frameworks, you will just miss out on some of the functionality.
- **RothDeveloper** - An application (WAR) that provides a basic database interface to work with any defined datasources configured in the Tomcat connection pool.
- **roth-deployment-package** - The parent project for building using Maven.

Requirements
------------

- JDK 21 or newer
- Tomcat 11 or newer
- A database (MySQL, Oracle, PostgreSQL, MS SQL Server, Informix, or DB/2)

An open-source DB like MySQL or PostgreSQL is suggested, but if you want to pay for the others...
Note that the database where Roth configuration and user management resides does not need to be the database where appliction data resides; it doesn't even have to be the same DBMS.

While Roth has been successfully used by 2 international companies over the years, there is no warranty, implied or otherwise.  You are responsible for thouroughly testing your own code before pushing to a production environment.  I am open to hearing from any users of this framework, and welcome suggestions or code contributions.  The more use it gets, the better it gets (that's been how it has worked over the last 15 years).

Getting Started
---------------

As mentioned before, the projects were developed with Eclipse in Windows, so this README and other documentation will center around that; translate as needed for your IDE of choice.

While you don't need to take our suggestion, we have found that making independent environment folders allows us to work with multiple environments at once.

Suggested Folder Structure
- C:\
  - development
    - environment-1
      - apache-tomcat-11.0.10
      - eclipse
      - workspace

- Create your folder struction
- Download the latest Eclipse J2EE zip file, and unarchive it
- Download the latest Tomcat 11 zip file, and unarchive it
- Run Eclipse and select your workspace folder (check the box to not ask again on startup)
- Create a server in Eclipse ( Apache / Tomcat v11.0 Server )
  - See Tomcat configuration notes below
- Open the Git pserspective
- Clone the repository
- Check out the projects
- Add Roth and RothDeveloper to the server
- Build using Maven (right-click the roth-deployment-package project and select Run As / Maven install
- Start your server

Tomcat Configuration Notes (conf folder)
----------------------------------------

***server.xml***
- Set your ports - the scheme we use is to set the 2nd digit to something incremental, so that you can configure up to 10 environments that can run simultaneously (if you so choose); usually we run 1 or 2 at a time
  - ```
    <Server port="8105" shutdown="SHUTDOWN">
    ```
  - ```
    <Connector connectionTimeout="20000" maxParameterCount="1000" port="8180" protocol="HTTP/1.1" redirectPort="8143"/>
    ```
  - ```
    <Connector port="8109" protocol="AJP/1.3" redirectPort="8143" secretRequired="false"/> <!-- A prod environment would have a secret required. -->
    ```
  - If you're not using a proxy (like Apache HTTPD), then you'll also need to set up the SSL/TLS connector on port 8143.
- Within the <GlocalNamingResources> body, you'll need to set up at least the Roth configuration/user management database (this example is using MySQL)
  - ```
    <Resource name="roth" type="javax.sql.DataSource" driverClassName="com.mysql.cj.jdbc.Driver" auth="Container"
              maxAllowedPacket="26214400" maxIdle="1" maxTotal="10" maxWaitMillis="10000"
              url="jdbc:mysql://localhost:3306/roth?verifyServerCertificate=false&amp;useSSL=false&amp;allowPublicKeyRetrieval=true&amp;useLegacyDatetimeCode=false&amp;nullDatabaseMeansCurrent=true" 
              username="roth" password="YourPasswordHere"/>
    ```
- Remove the existing Realm tag and add this
  - ```
    <Realm className="com.roth.realm.RothLockOutRealm">
      <Realm className="com.roth.realm.RothRealm"/>
    </Realm>
    ```
      
***context.xml***
- Add resource links for any database resources you set up in *server.xml*
  - ```
    <ResourceLink name="roth" global="roth" type="javax.sql.DataSource"/>
    ```
- Add the Roth tmp folder (make sure to also create the folder on the drive); this keeps it separate from Tomcat's own temp folder which could interfere if you used the same folder
  - ```
    <Environment name="rothTemp" override="false" type="java.lang.String" value="${catalina.base}/tmp/"/>
    ```
- For development environments, the cssCache should be false, but for production environments setting to true can help performance.  What is it?  Roth automatically transpiles SASS/SCSS into CSS at runtime.  Caching prevents it doing the transpile for every request (though to be honest, its fast even so).
  - ```
    <Environment name="cssCache" value="false" type="java.lang.Boolean" override="false"/>
    ```

***catalina.properties***
- Change the common.loader value to look like this
  - ```
    common.loader="${catalina.base}/lib","${catalina.base}/lib/*.jar","${catalina.home}/lib","${catalina.home}/lib/*.jar","${catalina.base}/lib.custom","${catalina.base}/lib.custom/*.jar"
    ```
- Make sure to add any JARs that don't have tld files in them to the tomcat.util.scan.StandardJarScanFilter.jarsToSkip value
  - ```
    tomcat.util.scan.StandardJarScanFilter.jarsToSkip=\
    annotations-api.jar,\
    ant-junit*.jar,\
    ant-launcher*.jar,\
    ant*.jar,\
    asm-*.jar,\
    aspectj*.jar,\
    bcel*.jar,\
    biz.aQute.bnd*.jar,\
    bootstrap.jar,\
    catalina-ant.jar,\
    catalina-ha.jar,\
    catalina-ssi.jar,\
    catalina-storeconfig.jar,\
    catalina-tribes.jar,\
    catalina.jar,\
    cglib-*.jar,\
    cobertura-*.jar,\
    commons-beanutils*.jar,\
    commons-codec*.jar,\
    commons-collections*.jar,\
    commons-compress*.jar,\
    commons-daemon.jar,\
    commons-dbcp*.jar,\
    commons-digester*.jar,\
    commons-fileupload*.jar,\
    commons-httpclient*.jar,\
    commons-io*.jar,\
    commons-lang*.jar,\
    commons-logging*.jar,\
    commons-math*.jar,\
    commons-pool*.jar,\
    derby-*.jar,\
    dom4j-*.jar,\
    easymock-*.jar,\
    ecj-*.jar,\
    el-api.jar,\
    geronimo-spec-jaxrpc*.jar,\
    h2*.jar,\
    ha-api-*.jar,\
    hamcrest-*.jar,\
    hibernate*.jar,\
    httpclient*.jar,\
    icu4j-*.jar,\
    jakartaee-migration-*.jar,\
    jasper-el.jar,\
    jasper.jar,\
    jaspic-api.jar,\
    jaxb-*.jar,\
    jaxen-*.jar,\
    jaxws-rt-*.jar,\
    jdom-*.jar,\
    jetty-*.jar,\
    jmx-tools.jar,\
    jmx.jar,\
    jsp-api.jar,\
    jstl.jar,\
    jta*.jar,\
    junit-*.jar,\
    junit.jar,\
    log4j*.jar,\
    mail*.jar,\
    objenesis-*.jar,\
    oraclepki.jar,\
    org.hamcrest.core_*.jar,\
    org.junit_*.jar,\
    oro-*.jar,\
    servlet-api-*.jar,\
    servlet-api.jar,\
    slf4j*.jar,\
    taglibs-standard-spec-*.jar,\
    tagsoup-*.jar,\
    tomcat-api.jar,\
    tomcat-coyote.jar,\
    tomcat-dbcp.jar,\
    tomcat-i18n-*.jar,\
    tomcat-jdbc.jar,\
    tomcat-jni.jar,\
    tomcat-juli-adapters.jar,\
    tomcat-juli.jar,\
    tomcat-util-scan.jar,\
    tomcat-util.jar,\
    tomcat-websocket.jar,\
    tools.jar,\
    unboundid-ldapsdk-*.jar,\
    websocket-api.jar,\
    websocket-client-api.jar,\
    wsdl4j*.jar,\
    xercesImpl.jar,\
    xml-apis.jar,\
    xmlParserAPIs-*.jar,\
    xmlParserAPIs.jar,\
    xom-*.jar\
    angus*.jar,\
    aopalliance*.jar,\
    avalon*.jar,\
    bc*.jar,\
    closure*.jar,\
    commons-*.jar,\
    error*.jar,\
    fontbox-*.jar,\
    fr.opensagres.*.jar,\
    guava-*.jar,\
    hk2*.jar,\
    httpcore*.jar,\
    itext-*.jar,\
    jai-imageio-*.jar,\
    jakarta.json-*.jar,\
    jakarta.activation-*.jar,\
    jakarta.mail-*.jar,\
    jakarta.xml*.jar,\
    jackson*.jar,\
    jersey*.jar,\
    jsoup-*-javadoc.jar,\
    jsoup-*-sources.jar,\
    jsoup-*.jar,\
    jsr*.jar,\
    junit-*.jar,\
    listenable*.jar,\
    mysql-connector*.jar,\
    oci*.jar,\
    ojdbc*.jar,\
    osgi*.jar,\
    owasp-java-html-sanitizer-*.jar,\
    pdfbox-*.jar,\
    pdfbox-tools-*.jar,\
    poi-*.jar,\
    poi-ooxml-*.jar,\
    preflight-*.jar,\
    protobuf*.jar,\
    resilience4j*.jar,\
    roth-lib-src.jar,\
    tika-*.jar,\
    vavr*.jar,\
    xmlbeans-*.jar,\
    xmpbox-*.jar,\
    yasson-*.jar
    ```
- Make sure to include the following in the tomcat.util.scan.StandardJarScanFilter.jarsToScan
  - ```
    tomcat.util.scan.StandardJarScanFilter.jarsToScan=\
    llog4j-taglib*.jar,\
    log4j-jakarta-web*.jar,\
    log4javascript*.jar,\
    slf4j-taglib*.jar\
    roth-lib.jar,\
    taglibs-standard-*.jar,\
    el-api.jar
    ```

***rothlog.json***
- This isn't required, as it'll work without it, but default logging only logs to stdout and stderr (i.e., catalina.out).  There will always be one global logger (i.e., the catch-all), but you can define other loggers that redirect specific log entries to other files using independent settings.  This example shows all logging from RothRealm will go to the file "realm.log".
  See documentation for more information on how to use it.
  ```
   {
  	"loggers": [
  		{
  			"logFilename": "/development/environment-1/apache-tomcat-11.0.10/logs/realm.log",
  			"logLevel": "EXCEPTION",
  			"logFilters": [
  				{
  					"filter": "com.roth.realm.RothRealm"
  				}
  			]
  		}
  	]
  }
  ```
