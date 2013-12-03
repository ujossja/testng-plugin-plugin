package hudson.plugins.jasonng.util;

import java.util.List;

import hudson.model.AbstractBuild;
import hudson.plugins.jasonng.JasonNGTestResultBuildAction;
import hudson.plugins.jasonng.results.ClassResult;
import hudson.plugins.jasonng.results.MethodResult;
import hudson.plugins.jasonng.results.JasonNGResult;

/**
 * Utility methods around displaying results (esp history of results)
 *
 * @author nullin
 */
public class TestResultHistoryUtil {

   /**
    * Gets the latest build before this one and returns it's test result.
    *
    * We'd rather make this list when needed otherwise if we cache these values
    * in the memory we will run out of memory
    *
    * @return previous build test results if the build exists and has a
    *       {@link hudson.plugins.jasonng.JasonNGTestResultBuildAction},
    *       otherwise returns an empty {@link hudson.plugins.jasonng.results.JasonNGResult}
    *       object. <b>Never returns {@code null}.</b>
    */
   public static JasonNGResult getPreviousBuildTestResults(AbstractBuild<?, ?> owner) {
      // Doesn't make sense to return a build that is still running.
      // We can compare results with a previous build that completed
      AbstractBuild<?, ?> previousBuild = owner.getPreviousCompletedBuild();
      if (previousBuild != null
               && previousBuild.getAction(JasonNGTestResultBuildAction.class) != null) {
         return previousBuild.getAction(JasonNGTestResultBuildAction.class).getResult();
      } else {
         return new JasonNGResult();
      }
   }

   /**
    * Summarizes the delta in tests and also displays a list of failed/skipped
    * tests and configuration methods.
    *
    * The list is returned as an HTML unordered list.
    *
    * @param action TestNG build action
    * @return summarized
    */
   //TODO: move into Groovy/Jelly
   public static String toSummary(JasonNGTestResultBuildAction action) {
      int prevFailedTestCount;
      int prevSkippedTestCount;
      int prevFailedConfigurationCount;
      int prevSkippedConfigurationCount;
      int prevTotalTestCount;

      AbstractBuild<?,?> owner = action.owner;
      JasonNGResult previousResult =
            TestResultHistoryUtil.getPreviousBuildTestResults(owner);

      prevFailedTestCount = previousResult.getFailCount();
      prevSkippedTestCount = previousResult.getSkipCount();
      prevFailedConfigurationCount = previousResult.getFailedConfigCount();
      prevSkippedConfigurationCount = previousResult.getSkippedConfigCount();
      prevTotalTestCount = previousResult.getTotalCount();

      JasonNGResult tr = action.getResult();

      return "<ul>" + diff(prevTotalTestCount, tr.getTotalCount(), "Total Tests")
            + diff(prevFailedTestCount, tr.getFailCount(), "Failed Tests")
            + printTestsUrls(owner, tr.getFailedTests())
            + diff(prevSkippedTestCount, tr.getSkipCount(), "Skipped Tests")
            + printTestsUrls(owner, tr.getSkippedTests())
            + diff(prevFailedConfigurationCount, tr.getFailedConfigCount(), "Failed Configurations")
            + printTestsUrls(owner, tr.getFailedConfigs())
            + diff(prevSkippedConfigurationCount, tr.getSkippedConfigCount(), "Skipped Configurations")
            + printTestsUrls(owner, tr.getSkippedConfigs())
            + "</ul>";
   }

   private static String diff(long prev, long curr, String name) {
      if (prev == curr) {
         return "<li>" + name + ": " + curr + " (&plusmn;0)</li>";
      } else if (prev < curr) {
         return "<li>" + name + ": " + curr + " (+" + (curr - prev) + ")</li>";
      } else { // if (a < b)
         return "<li>" + name + ": " + curr + " (-" + (prev - curr) + ")</li>";
      }
   }

   /*
      <OL start="10">
         <LI><a href="url">test_full_name</a></LI>
      </OL>
   */
   private static String printTestsUrls(AbstractBuild<?,?> owner, List<MethodResult> methodResults) {
      StringBuffer htmlStr = new StringBuffer();
      htmlStr.append("<OL>");
      if (methodResults != null && methodResults.size() > 0) {
         for (MethodResult methodResult : methodResults) {
            htmlStr.append("<LI>");
            if (methodResult.getParent() instanceof ClassResult) {
               htmlStr.append("<a href=\"").append(methodResult.getUpUrl());
               htmlStr.append("\">");
               htmlStr.append(methodResult.getParent().getName());
               htmlStr.append(".").append(methodResult.getName()).append("</a>");
            } else {
               htmlStr.append(methodResult.getName());
            }
            htmlStr.append("</LI>");
         }

      }
      htmlStr.append("</OL>");
      return htmlStr.substring(0);
   }

}
