package io.mindspice.outerfieldsserver.core.config;

import org.springframework.context.annotation.Configuration;


@Configuration
public class CombatInitialization {

    /* Handle some initialization settings */

//    @Bean
//    public CommandLineRunner customInitialization() {
//        return args -> {
//            ObjectMapper mapper = JsonUtils.getMapper();
//            mapper.disable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
//        };
//    }
//

    /* Core Player Data */



    /* Network Services */


//    @Bean
//    CombatSystemSocketHandler CombatSystemSocketHandler(
//            @Qualifier("playerTable") ConcurrentHashMap<Integer, Player> playerTable,
//            @Qualifier("gameApi") OkraGameAPI gameApi) {
//        return new CombatSystemSocketHandler(playerTable, gameApi);
//    }
//
//    @Bean
//    TokenHandshakeInterceptor tokenHandshakeInterceptor(
//            @Qualifier("httpServiceClient") HttpServiceClient httpServiceClient) {
//        return new TokenHandshakeInterceptor(httpServiceClient);
//    }

//    @Bean GameRestController gameRestController(
//            @Qualifier("httpServiceClient") HttpServiceClient httpServiceClient,
//            @Qualifier("CombatSystem") CombatSystem CombatSystem,
//            @Qualifier("playerTable") ConcurrentHashMap<Integer, Player> playerTable) {
//        return new GameRestController(httpServiceClient,CombatSystem, playerTable);
//    }


}
