package io.mindspice.outerfieldsserver.combat.cards;

import java.util.HashMap;


public class CardBalance {
    HashMap<Integer, String> info = new HashMap<>();

 //   @Test
//    void weaponCards() throws JsonProcessingException {
//        for (var card : WeaponCard.values()) {
//            var cost = card.getStats().getCost().getSum();
//            var damage = card.getStats().getHpDamage();
//            var selfDamageDamage = card.getStats().getSelfDamage().asMap().get(StatType.HP);
//
//            var json = new JsonUtils.ObjectBuilder()
//                    .put("Card", card)
//                    .put("Level", card.getLevel())
//                    .put("Cost", cost)
//                    .put("damage_per_cost", (damage - selfDamageDamage) / cost)
//                    .put("damage_per_level", (damage - selfDamageDamage) / card.getLevel())
//                    .put("total_damage", (damage - selfDamageDamage) * card.getStats().getDamage().chance + card.getStats().getDamageCalc())
//                    .buildNode();
//
//            info.put((damage - selfDamageDamage), JsonUtils.writePretty(json));
//        }
//        List<Map.Entry<Integer, String>> sortedEntries = info.entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .toList();
//
//        sortedEntries.forEach(e -> System.out.println(e.getValue()));
//    }


}
