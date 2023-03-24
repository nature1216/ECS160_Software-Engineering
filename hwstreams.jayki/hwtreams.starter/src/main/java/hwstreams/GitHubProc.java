package hwstreams;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class GitHubProc {
  public static Long getWordCount(Stream<GitHubComment> stream, String word) {
    return Arrays.stream(Util.getWords(stream.map(GitHubComment::body).collect(joining()))).filter(str -> str.equals(word)).count();
  }

  private static Long getSingleWordCount(GitHubComment comment, String word) {
    return Arrays.stream(Util.getWords(comment.body())).filter(str -> str.equals(word)).count();
  }

  public static Map<String, Long> getPerProjectCount(Stream<GitHubComment> stream) {
    return stream.collect(groupingBy(Util::getProject,counting()));
  }

  public static Map<String, Long> getAuthorActivity(Stream<GitHubComment> stream) {
    return stream.collect(groupingBy(GitHubComment::author,counting()));
  }

  public static Map<String, Long> getCommentUrlAuthorCount(Stream<GitHubComment> stream) {
    return stream.filter(group -> {
      return group.body().contains("https://") || group.body().contains("http://");
    }).collect(groupingBy(GitHubComment::author,counting()));
  }

  public static Stream<GitHubComment> filterCommentsWithUrl(Stream<GitHubComment> comments) {
    return comments.filter(comment -> {
      return comment.body().contains("https://") || comment.body().contains("http://");
    });
  }

  public static Map<String, Double> getAuthorAverageVerbosity(Stream<GitHubComment> stream) {
    return stream.collect(groupingBy(GitHubComment::author,averagingDouble(comment -> {
      return Util.getWords(comment.body()).length;
    })));
  }

  public static Map<String, Map<String, Long>> getAuthorWordCountPerProject(
      Stream<GitHubComment> stream, String word) {

    return stream.collect(groupingBy(Util::getProject, groupingBy(GitHubComment::author, counting())));
    }
}
