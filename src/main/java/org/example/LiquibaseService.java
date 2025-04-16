package org.example;

import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LiquibaseService {

    private final Path basePath = Path.of("liquibase/changesets");

    public void processNotifications(List<Notification> notifications) {
        String content = generateSql(notifications);
        Path filePath = saveToFile(content);
        commitToGit(filePath);
    }

    public String generateSql(List<Notification> notifications) {
        StringBuilder sb = new StringBuilder();
        String author = System.getProperty("user.name");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        sb.append("-- changeset ").append(author).append(":").append(timestamp).append("\n");

        for (Notification n : notifications) {
            sb.append("INSERT INTO NOTIFICATION_TABLE (ID, TITLE, BODY, OPERATION_ID) VALUES (")
                    .append("SEQ_NOTIFICATION_ID.NEXTVAL, ")
                    .append("'").append(escape(n.getTitle())).append("', ")
                    .append("'").append(escape(n.getBody())).append("', ")
                    .append(n.getOperationId() == null || n.getOperationId().isBlank() ? "NULL" : "'" + escape(n.getOperationId()) + "'")
                    .append(");\n");
        }

        return sb.toString();
    }

    private String escape(String value) {
        return value.replace("'", "''");
    }

    @SneakyThrows
    private Path saveToFile(String content) {
        if (!Files.exists(basePath)) {
            Files.createDirectories(basePath);
        }
        String fileName = "insert_notifications_" + System.currentTimeMillis() + ".sql";
        Path path = basePath.resolve(fileName);
        Files.writeString(path, content, StandardOpenOption.CREATE_NEW);
        return path;
    }

    @SneakyThrows
    private void commitToGit(Path filePath) {
        File repoDir = new File("."); // корень git-репозитория
        try (Git git = Git.open(repoDir)) {
            git.add().addFilepattern(basePath.relativize(filePath).toString()).call();
            git.commit().setMessage("Add notification changeset").call();
            git.push().call();
        } catch (GitAPIException e) {
            throw new RuntimeException("Git error: " + e.getMessage(), e);
        }
    }
}
