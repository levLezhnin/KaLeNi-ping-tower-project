package team.kaleni.ping.tower.backend.statistics_service.config;

import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.client.ClickHouseCredentials;
import com.clickhouse.client.ClickHouseNode;
import com.clickhouse.client.ClickHouseProtocol;
import com.clickhouse.client.config.ClickHouseClientOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ClickhouseConfig {

    @Value("${clickhouse.url}")
    private String clickhouseUrl;

    @Value("${clickhouse.database}")
    private String database;

    @Value("${clickhouse.username}")
    private String username;

    @Value("${clickhouse.password}")
    private String password;

    @Value("${clickhouse.connection-timeout:10000}")
    private int connectionTimeout;

    @Value("${clickhouse.socket-timeout:30000}")
    private int socketTimeout;

    @Bean
    public ClickHouseClient clickHouseClient() {
        log.info("Initializing ClickHouse client for statistics service with URL: {}", clickhouseUrl);

        return ClickHouseClient.builder()
                .option(ClickHouseClientOption.CONNECTION_TIMEOUT, connectionTimeout)
                .option(ClickHouseClientOption.SOCKET_TIMEOUT, socketTimeout)
                .option(ClickHouseClientOption.COMPRESS, false) // Отключено для стабильности
                .option(ClickHouseClientOption.DECOMPRESS, false)
                .build();
    }

    @Bean
    public ClickHouseNode clickHouseNode() {
        String nodeUrl = clickhouseUrl.replace("http://", "").replace("https://", "");
        String[] parts = nodeUrl.split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 8123;

        ClickHouseNode node = ClickHouseNode.builder()
                .host(host)
                .port(ClickHouseProtocol.HTTP, port)
                .database(database)
                .credentials(ClickHouseCredentials.fromUserAndPassword(username, password))
                .build();

        log.info("Created ClickHouse node for statistics: {}:{} database: {}", host, port, database);
        return node;
    }
}
