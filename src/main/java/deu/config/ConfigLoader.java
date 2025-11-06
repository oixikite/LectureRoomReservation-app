package deu.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.*;
import java.util.function.Consumer;

public class ConfigLoader {

    private static volatile Config configInstance;
    private static final String DEST_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "config.yaml";
    private static volatile boolean watcherStarted = false;

    private ConfigLoader() {}

    // 설정 조회
    public static Config getConfig() {
        if (configInstance == null) {
            synchronized (ConfigLoader.class) {
                if (configInstance == null) {
                    ensureConfigFileExists();
                    configInstance = load();
                }
            }
        }

        // 최초 한 번만 감시 스레드 시작
        if (!watcherStarted) {
            synchronized (ConfigLoader.class) {
                if (!watcherStarted) {
                    watchConfigFile();
                    watcherStarted = true;
                }
            }
        }

        return configInstance;
    }

    // 설정 수정 및 저장
    public static void updateConfig(Consumer<Config> modifier) {
        synchronized (ConfigLoader.class) {
            ensureConfigFileExists();
            if (configInstance == null) {
                configInstance = load();
            }

            modifier.accept(configInstance);
            save(configInstance);
        }
    }

    // 설정 저장
    private static void save(Config config) {
        try (Writer writer = new FileWriter(DEST_PATH)) {
            DumperOptions options = new DumperOptions();
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

            Representer representer = new Representer(options);
            representer.getPropertyUtils().setSkipMissingProperties(true);
            representer.addClassTag(Config.class, Tag.MAP); // !!deu.config.Config 제거

            Yaml yaml = new Yaml(representer, options);
            yaml.dump(config, writer);
        } catch (IOException e) {
            throw new RuntimeException("config.yaml 저장 실패", e);
        }
    }

    // 설정 로딩
    private static Config load() {
        try (InputStream in = new FileInputStream(DEST_PATH)) {
            Yaml yaml = new Yaml(); // 클래스 태그가 없는 경우 기본 Yaml로도 잘 동작
            return yaml.loadAs(in, Config.class);
        } catch (Exception e) {
            throw new RuntimeException("config.yaml 로딩 실패", e);
        }
    }

    // 리소스에서 복사
    private static void ensureConfigFileExists() {
        File configFile = new File(DEST_PATH);
        if (configFile.exists()) return;

        try (InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream("config.yaml")) {
            if (in == null) {
                throw new FileNotFoundException("resources/config.yaml 파일을 찾을 수 없습니다.");
            }

            Path parentDir = configFile.toPath().getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            Files.copy(in, configFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("초기 config.yaml 복사 실패", e);
        }
    }

    // 설정 파일 변경 감지
    private static void watchConfigFile() {
        Path configPath = Path.of(DEST_PATH);
        Path parentDir = configPath.getParent();

        Thread watcherThread = new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                parentDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take(); // blocking
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        Path changed = (Path) event.context();

                        if (kind == StandardWatchEventKinds.ENTRY_MODIFY &&
                                changed.getFileName().toString().equals("config.yaml")) {

                            synchronized (ConfigLoader.class) {
                                configInstance = load();
                                System.out.println("[ConfigLoader] config.yaml 변경 감지 → 설정 자동 재로드");
                            }
                        }
                    }
                    key.reset();
                }
            } catch (Exception e) {
                System.err.println("[ConfigLoader] 감시 중 오류 발생: " + e.getMessage());
            }
        });

        watcherThread.setDaemon(true);
        watcherThread.start();
    }
}