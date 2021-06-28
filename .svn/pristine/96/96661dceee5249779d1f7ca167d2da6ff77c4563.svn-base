@echo off
rem ex. run client
rem start "JCP Layer1" /B javaw -jar JCP-Layer1.jar tw.com.hyweb.online.DefMain default

rem Factors that affect JVM performance
rem Heap and stack sizes (-Xms, -Xmx, -Xss, and -Xoss settings).
rem The search path to the class libraries (classpath  and  most-used  classes  should come first).
rem Garbage  collection  activity.
rem The quality of the application code.
rem Just-in-Time compiler.
rem The machine configuration:
rem ¡V I/O  disk  size  and  speed
rem ¡V Number  and  speed  of  CPUs
rem ¡V Processor  cache  size  and  speed
rem ¡V Random  access  memory  size  and  speed
rem ¡V Network  and  network  adapters  number  and  speed

rem for show gc information
rem -verbose:gc

rem for log gc information
rem -Xloggc:gc.log

rem for multi cpu and reduct pause time
rem -Xgcpolicy:optavgpause

rem for multi cpu and reduct pause time (mix mod with current and general)
rem -Xgcpolicy:gencon

rem for jprofiler
rem LIBPATH=$LIBPATH:/home/edp81438/jprofiler5/bin/aix-ppc64;export LIBPATH
rem -agentlib:jprofilerti= -Xbootclasspath/a:/home/edp81438/jprofiler5/bin/agent.jar

rem for JMX jconsole
rem -Dcom.sun.management.jmxremote

rem for log4j configuration
rem -Dlog4j.debug=true -Dlog4j.configuration="file:/workspace/boccc/config/loyalty/log4j.xml"

set CP=.
for %%i in ("lib\*.jar") do call "setcp.bat" %%i
for %%i in ("bin\*.jar") do call "setcp.bat" %%i
for %%i in ("lib\dom4j-1.6.1\*.jar") do call "setcp.bat" %%i
for %%i in ("lib\jaxb\*.jar") do call "setcp.bat" %%i
for %%i in ("lib\javamail-1.3.2\*.jar") do call "setcp.bat" %%i

%JAVA_HOME%\bin\java -Djava.endorsed.dirs="lib\endorsed" -Dlog4j.configuration="config/loyalty/log4.xml" -Xms96M -Xmx256M -cp %CP% tw.com.hyweb.online.DefMain %1 %2
rem java -Dlog4j.configuration="config/loyalty/log4.xml" -Xloggc:gc.log -Xms96M -Dcom.sun.management.jmxremote -cp .;build\classes;lib\dom4j-1.6.1\dom4j-1.6.1.jar;lib\javamail-1.3.2\activation.jar;lib\activeio-core-3.0-beta1.jar;lib\activemq-core-4.0-RC2.jar;lib\backport-util-concurrent-2.1.jar;lib\campaign.jar;lib\commons-cli-1.0.jar;lib\commons-collections-3.1.jar;lib\commons-dbcp-1.2.1.jar;lib\commons-io-1.0.jar;lib\commons-lang-2.1.jar;lib\commons-logging.jar;lib\commons-net-1.4.1.jar;lib\commons-pool-1.2.jar;lib\dbunit-2.2.jar;lib\dbunit-2.2-javadoc.zip;lib\dbunit-2.2-sources.jar;lib\geronimo-j2ee-management_1.0_spec-1.0.jar;lib\geronimo-jms_1.1_spec-1.0.jar;lib\hyweb-dao.jar;lib\hyweb-layer1.jar;lib\hyweb-layer2.jar;lib\hyweb-layer3-BOCCC.jar;lib\jakarta-oro-2.0.8.jar;lib\jpos.jar;lib\junit.jar;lib\log4j-1.2.13.jar;lib\ojdbc14.jar;lib\spring.jar;lib\swt.jar;lib\swt-nl.jar;lib\dom4j-1.6.1\dom4j-1.6.1-doc.zip;lib\dom4j-1.6.1\isorelax-20030108.jar;lib\dom4j-1.6.1\jaxen-1.1-beta-6.jar;lib\dom4j-1.6.1\jaxme-0.3.jar;lib\dom4j-1.6.1\jaxme-api-0.3.jar;lib\dom4j-1.6.1\jaxme-js-0.3.jar;lib\dom4j-1.6.1\jaxme-xs-0.3.jar;lib\dom4j-1.6.1\jsr173_1.0_api.jar;lib\dom4j-1.6.1\jtidy-4aug2000r7-dev.jar;lib\dom4j-1.6.1\msv-20030807.jar;lib\dom4j-1.6.1\pull-parser-2.1.10.jar;lib\dom4j-1.6.1\relaxngDatatype-20030807.jar;lib\dom4j-1.6.1\xalan-2.5.1.jar;lib\dom4j-1.6.1\xercesImpl-2.6.2.jar;lib\dom4j-1.6.1\xml-apis-2.0.2.jar;lib\dom4j-1.6.1\xpp3-1.1.3.3.jar;lib\dom4j-1.6.1\xsdlib-20030807.jar;lib\javamail-1.3.2\imap.jar;lib\javamail-1.3.2\mailapi.jar;lib\javamail-1.3.2\pop3.jar;lib\javamail-1.3.2\smtp.jar;lib\commons-codec-1.3.jar;lib\CommonLite.jar tw.com.hyweb.online.DefMain %1 %2
