package hudson.plugins.jasonng;

import hudson.Plugin;
import hudson.model.Run;

/**
 * Entry point of TestNG Results plugin.
 *
 * TODO: should move to newer supported way of initializing plugins
 */
public class PluginImpl extends Plugin {

    public static final String DISPLAY_NAME = "JasonNG Results";
    public static final String GRAPH_NAME = "JasonNG Results Trend";
    public static final String URL = "testngreports";
    public static final String ICON_FILE_NAME = "/plugin/testng-plugin/icons/report.png";

    public void start() throws Exception {
        Run.XSTREAM.registerConverter(new BuildActionConverter());
        //this is the name with which older build actions are stored with in build.xml files
        //here for backward compatibility
        Run.XSTREAM.alias("hudson.plugins.jasonng.JasonNGBuildAction", JasonNGTestResultBuildAction.class);
        //this will be written to the build.xml file when saving TestNG build action
        Run.XSTREAM.alias("jasonngBuildAction", JasonNGTestResultBuildAction.class);
    }
}
