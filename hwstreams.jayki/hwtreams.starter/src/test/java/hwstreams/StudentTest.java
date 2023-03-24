package hwstreams;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class StudentTest {
    Stream<GitHubComment> getTestData() {
        return Stream.of(
                new GitHubComment(
                        "22422684",
                        "a561c2df715a3c24059fd06136d408c24903a460",
                        "https://github.com/dnanexus/dx-toolkit/pull/86#discussion_r22422684",
                        "psung",
                        "2015-01-02T21:01:02Z",
                        "I realized I can't modify the exception being thrown without resetting the call stack to that of the new throw site  "
                        + "and I didn't really want to go down that road (this relates to both the recent change to preserve the call stack properly  "
                        + "and also our other discussion about if there were a more useful way to chain exceptions)."
                        + "In order to fix this test I've started catching the exception that's raised and checking that it passes `dxpy._is_retryable_exception` (but leaving the exception untouched in `DXHTTPRequest`)."
                ),
                new GitHubComment(
                        "22403257",
                        "d796cb93cda0d5dc9590f8feffc2c776b502f177",
                        "https://github.com/dnanexus/dx-toolkit/pull/86#discussion_r22403257",
                        "psung",
                        "2015-01-02T00:19:33Z",
                        "Indeed. But even without the special syntax is there no Python convention to retrieve the original exception? "
                        + "I guess what I had in mind was like this in Java: http://docs.oracle.com/javase/7/docs/api/java/lang/Throwable.html#getCause()"
                ),
                new GitHubComment("22403269",
                        "d796cb93cda0d5dc9590f8feffc2c776b502f177",
                        "https://github.com/dnanexus/dx-toolkit/pull/86#discussion_r22403269",
                        "psung",
                        "2015-01-02T00:22:17Z",
                        "Indeed  one way out may also be to keep the retry behavior the same but to print name resolution failures to stderr."
                ),
                new GitHubComment(
                        "22416081",
                        "79864cb33b87564a81afdac46d0f9ac605ae5e57",
                        "https://github.com/zendframework/zf2/pull/7093#discussion_r22416081",
                        "larsnystrom","2015-01-02T17:33:55Z",
                        "That method overrides `setObjectKey()` in Partial  which we inherit. "
                        + "Maybe I should've added that to the docblock  but I'm unsure of the syntax."
                ),
                new GitHubComment(
                        "22417569",
                        "4a61055384a40482ab4040a5584ce87406a53d42",
                        "https://github.com/poetic/butler-api/pull/15#discussion_r22417569",
                        "jakecraige",
                        "2015-01-02T18:24:19Z",
                        "For all these auth params below  you should be able to use the helper "
                        + "I provided `auth_params`. see [here](https://github.com/poetic/butler-api/blob/master/spec/requests/api/v1/reports_spec.rb#L11-L16) for it being used"
                ));
    }

    @Test
    public void studentShouldTest() { // TODO: rename test name to something meaningful
        var authorWordCountTestMap = GitHubProc.getWordCount(getTestData(), "but");
        assertEquals(2,authorWordCountTestMap); // TODO: implement a non-trivial test

        var commentsPerProjectTestMap = GitHubProc.getPerProjectCount(getTestData());
        var commentsPerProjectExpectedMap = new HashMap<String, Long>();
        commentsPerProjectExpectedMap.put("dnanexus/dx-toolkit", 3L);
        commentsPerProjectExpectedMap.put("poetic/butler-api", 1L);
        commentsPerProjectExpectedMap.put("zendframework/zf2", 1L);
        assertEquals(commentsPerProjectExpectedMap, commentsPerProjectTestMap);

        var commentsPerAuthorTestMap = GitHubProc.getAuthorActivity(getTestData());
        var commentsPerAuthorExpectedMap = new HashMap<String, Long>();
        commentsPerAuthorExpectedMap.put("jakecraige", 1L);
        commentsPerAuthorExpectedMap.put("psung", 3L);
        commentsPerAuthorExpectedMap.put("larsnystrom", 1L);
        assertEquals(commentsPerAuthorExpectedMap, commentsPerAuthorTestMap);

        var authorWordCountPerProjectTestMap = GitHubProc.getAuthorWordCountPerProject(getTestData(), "helper");
        var authorWordCountPerProjectExpectedMap = new HashMap<String, Map<String, Long>>();
        authorWordCountPerProjectExpectedMap.put("dnanexus/dx-toolkit",
                new HashMap<String, Long>() {
                    {
                        put("psung", 3L);
                    }
                });
        authorWordCountPerProjectExpectedMap.put("poetic/butler-api",
                new HashMap<String, Long>() {
                    {
                        put("jakecraige", 1L);
                    }
                });
        authorWordCountPerProjectExpectedMap.put("zendframework/zf2",
                new HashMap<String, Long>() {
                    {
                        put("larsnystrom", 1L);
                    }
                });
        assertEquals(authorWordCountPerProjectExpectedMap, authorWordCountPerProjectTestMap);

        var authorAverageVerbosityTestMap = GitHubProc.getAuthorAverageVerbosity(getTestData());
        var authorAverageVerbosityExpectedMap = new HashMap<String, Double>();
        authorAverageVerbosityExpectedMap.put("jakecraige", 23.0);
        authorAverageVerbosityExpectedMap.put("psung", 45.666666666666664);
        authorAverageVerbosityExpectedMap.put("larsnystrom", 23.0);
        assertEquals(authorAverageVerbosityExpectedMap, authorAverageVerbosityTestMap);

        var filterCommentsWithUrlTestStream = GitHubProc.filterCommentsWithUrl(getTestData());
        var match =
                filterCommentsWithUrlTestStream.allMatch(
                        comment -> {
                            return comment.body().contains("https://") || comment.body().contains("http://");
                        });
        assertTrue(match);
    }
}
