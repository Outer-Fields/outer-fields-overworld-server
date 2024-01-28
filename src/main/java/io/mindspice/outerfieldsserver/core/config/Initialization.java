package io.mindspice.outerfieldsserver.core.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.databaseservice.client.DBServiceClient;
import io.mindspice.databaseservice.client.api.OkraGameAPI;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.outerfieldsserver.combat.bot.BotFactory;
import io.mindspice.outerfieldsserver.components.primatives.SimpleListener;
import io.mindspice.outerfieldsserver.core.HttpServiceClient;
import io.mindspice.outerfieldsserver.core.MatchMaking;
import io.mindspice.outerfieldsserver.core.Settings;
import io.mindspice.outerfieldsserver.core.shell.SchemeShell;
import io.mindspice.outerfieldsserver.core.systems.*;
import io.mindspice.outerfieldsserver.entities.*;
import io.mindspice.outerfieldsserver.area.ChunkJson;
import io.mindspice.outerfieldsserver.area.TileData;
import io.mindspice.outerfieldsserver.core.networking.SocketService;
import io.mindspice.outerfieldsserver.core.networking.websockets.GameServerSocketHandler;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.jctools.maps.NonBlockingHashMapLong;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mindspice.kawautils.wrappers.KawaInstance;

import java.io.File;
import java.io.IOException;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Configuration
public class Initialization {
    EntityManager entityManager = EntityManager.GET();

    @Bean
    public NonBlockingHashMapLong<PlayerEntity> playerTable() {
        return new NonBlockingHashMapLong<>(100);
    }

    public List<Pair<String, Class<?>>> getInnerClasses(Class<?> clazz) {// Replace with your class
        Class<?>[] innerClasses = clazz.getDeclaredClasses();

        List<Pair<String, Class<?>>> inners = new ArrayList<>(innerClasses.length);
        for (Class<?> innerClass : innerClasses) {
            String fullName = innerClass.getName();
            String packageName = innerClass.getPackage().getName();
            String className = fullName.substring(packageName.length() + 1).replace('$', '.');
            inners.add(Pair.of(className, innerClass));
        }
        return inners;
    }

    public List<Pair<String, Class<?>>> reflectAll() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("io.mindspice"))
                .setScanners(new SubTypesScanner(false)));

        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
        Set<Class<?>> inners = classes.stream()
                .flatMap(c -> Arrays.stream(c.getDeclaredClasses()))
                .collect(Collectors.toSet());

        classes.addAll(inners);
        Pattern anonPat = Pattern.compile(".*\\$\\d+$");
        classes.removeIf(c -> c.getName().contains("proto")
                || c.getName().contains("Scheme")
                || c.getName().contains("EntityManager")
                || anonPat.matcher(c.getName()).matches()
        );

        ArrayList<Pair<String, Class<?>>> mappedClasses = new ArrayList<>(classes.size());
        classes.stream().distinct().forEach(c -> {
            String fullName = c.getName();
            String packageName = c.getPackage().getName();
            String className = fullName.substring(packageName.length() + 1).replace('$', '.');
            System.out.println(className);
            mappedClasses.add(Pair.of(className, c));
        });
        // Some duplicates make it through the set due to naming difference, so remove them
        Set<String> seen = new HashSet<>();
        return mappedClasses.stream()
                .filter(pair -> seen.add(pair.first()))
                .collect(Collectors.toList());
    }

    @Bean
    public KawaInstance scheme() throws Throwable {
        KawaInstance kawa = new KawaInstance();
        kawa.defineObject("EntityManager", entityManager);
        reflectAll().forEach(p -> kawa.defineObject(p.first(), p.second()));
        var load1 = kawa.loadSchemeFile(new File("src/main/resources/scheme/std-aliases.scm"));
        var load2 = kawa.loadSchemeFile(new File("src/main/resources/scheme/std-functions.scm"));

        List.of(load1, load2).forEach(r -> {
            if (!r.valid()) {
                System.out.println("|-------------------------------|");
                System.out.println("| Exception loading scheme file |");
                System.out.println("|-------------------------------|");
                System.out.println(r.exception());
            }
        });
        System.out.println("|---------------------------|");
        System.out.println("| Loaded Scheme definitions |");
        System.out.println("|---------------------------|");
        return kawa;
    }

    private <E> BiConsumer<SimpleListener, Event<E>> getShellEventConsumer(Consumer<String> inputConsumer) {
        return BiPredicatedBiConsumer.of(
                (self, event) -> event.recipientEntityId() == self.entityId(),
                (self, event) -> {
                    try {
                        inputConsumer.accept(JsonUtils.writePretty(event));
                    } catch (JsonProcessingException e) {
                        inputConsumer.accept("Error on event:" + event.eventType() + "Error: " + e.getMessage());
                    }
                }
        );

    }

    @Bean
    public SchemeShell shell(@Qualifier("scheme") KawaInstance scheme) throws IOException {
        SchemeShell shell = new SchemeShell(2222, null, scheme);
        Consumer<String> inputConsumer = shell.getExternalInputConsumer();
        shell.addEventToggle(entityManager::toggleEventMonitoring);
        entityManager.linkEventMonitor(inputConsumer);
        shell.refreshCompletions();

        // this is registered below with the quest system, as it needs to be stuck somewhere. Later this should be added
        // to the external event system
        ShellEntity shellEntity = entityManager.getShellEntity();
        SimpleListener shellListener = new SimpleListener(shellEntity);
        Arrays.stream(EventType.values()).forEach(e -> shellListener.registerListener(e, getShellEventConsumer(inputConsumer)));
        shellEntity.addComponent(shellListener);

        scheme.defineObject("ShellListener", shellListener);
        shell.refreshCompletions();
        return shell;
    }

    @Bean
    public SocketService socketService() {
        return new SocketService(playerTable());
    }

    @Bean
    public GameServerSocketHandler CombatSystemSocketHandler(
            @Qualifier("playerTable") NonBlockingHashMapLong<PlayerEntity> playerTable,
            @Qualifier("socketService") SocketService socketService) {
        return new GameServerSocketHandler(socketService, playerTable);
    }

    @Bean
    public WorldSystem worldSystem() throws IOException {

        ChunkJson chunkJson = GridUtils.parseChunkJson(new File(
                "/home/mindspice/code/Java/Okra/outer-fields-overworld-server/src/main/resources/chunkdata/chunk_0_0.json")
        );

        AreaEntity area = EntityManager.GET().newAreaEntity(
                AreaId.TEST,
                IRect2.of(0, 0, 1920, 1920),
                IVector2.of(1920, 1920),
                List.of()
        );

        Map<IVector2, TileData> chunkData = ChunkEntity.loadFromJson(chunkJson);
        ChunkEntity[][] chunkMap = new ChunkEntity[1][1];
        chunkMap[0][0] = EntityManager.GET().newChunkEntity(AreaId.TEST, IVector2.of(0, 0), chunkJson);
        area.setChunkMap(chunkMap);

        area.addCollisionToGrid(chunkJson.collisionPolys());
        //   System.out.println(EntityManager.GET().areaById(AreaId.TEST));

        return entityManager.newWorldSystem(Map.of(AreaId.TEST, area));

    }

    @Bean
    public PlayerSystem playerSystem() {
        return entityManager.newPlayerSystem();
    }

    @Bean
    public QuestSystem questSystem() {
        QuestSystem questSystem = entityManager.newQuestSystem();
        ShellEntity shellEntity = entityManager.getShellEntity();
        shellEntity.registerWithSystem(questSystem); // FIXME register this else where later
        return questSystem;
    }

    @Bean
    public NPCSystem npcSystem() {
        return entityManager.newNPCSystem();

    }

    // COMBAT INITIALIZATION

    /* Core Game Services */

    @Bean
    ScheduledExecutorService combatExecutor() {
        return new ScheduledThreadPoolExecutor(Settings.GET().gameExecThreads);
    }

    @Bean
    DBServiceClient dbServiceClient() throws Exception {
        return new DBServiceClient(Settings.GET().dbURI, Settings.GET().dbUser, Settings.GET().dbPass);
    }

    @Bean
    OkraGameAPI gameApi(@Qualifier("dbServiceClient") DBServiceClient dbServiceClient) {
        return new OkraGameAPI(dbServiceClient);
    }

    @Bean
    HttpServiceClient httpServiceClient(@Qualifier("gameApi") OkraGameAPI gameApi) {
        return new HttpServiceClient(gameApi);
    }

    @Bean
    CombatSystem CombatSystem(
            @Qualifier("combatExecutor") ScheduledExecutorService gameExec,
            @Qualifier("httpServiceClient") HttpServiceClient httpServiceClient,
            @Qualifier("playerTable") NonBlockingHashMapLong<PlayerEntity> playerTable) {
        CombatSystem server = entityManager.newCombatSystem(gameExec, httpServiceClient, playerTable);
        //  server.init();
        return server;
    }

    @Bean
    MatchMaking matchMaking(
            @Qualifier("combatExecutor") ScheduledExecutorService gameExec,
            @Qualifier("playerTable") NonBlockingHashMapLong<PlayerEntity> playerTable,
            @Qualifier("httpServiceClient") HttpServiceClient httpServiceClient
    ) {
        MatchMaking matchMaking = new MatchMaking(combatExecutor(), playerTable, httpServiceClient);
        matchMaking.init();
        return matchMaking;
    }

}
