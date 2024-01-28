#| Enities |#

(define-alias Entity io.mindspice.outerfieldsserver.entities.Entity)
(define-alias AreaEntity io.mindspice.outerfieldsserver.entities.AreaEntity)
(define-alias ChunkEntity io.mindspice.outerfieldsserver.entities.ChunkEntity)
(define-alias ItemEntity io.mindspice.outerfieldsserver.entities.ItemEntity)
(define-alias LocationEntity io.mindspice.outerfieldsserver.entities.LocationEntity)
(define-alias LootEntity io.mindspice.outerfieldsserver.entities.LootEntity)
(define-alias NonPlayerEntity io.mindspice.outerfieldsserver.entities.NonPlayerEntity)
(define-alias PlayerEntity io.mindspice.outerfieldsserver.entities.PlayerEntity)
(define-alias PlayerQuestEntity io.mindspice.outerfieldsserver.entities.PlayerQuestEntity)
(define-alias PositionalEntity io.mindspice.outerfieldsserver.entities.PositionalEntity)
(define-alias SystemEntity io.mindspice.outerfieldsserver.entities.SystemEntity)
(define-alias WorldQuestEntity io.mindspice.outerfieldsserver.entities.WorldQuestEntity)

#| Geometry |#

;; Vector
(define-alias IConstVector2 io.mindspice.mindlib.data.geometry.IConstVector2)
(define-alias IMutVector2 io.mindspice.mindlib.data.geometry.IMutVector2)
(define-alias IAtomicVector2 io.mindspice.mindlib.data.geometry.IAtomicVector2)
(define-alias IVector2 io.mindspice.mindlib.data.geometry.IVector2)

;; Rect
(define-alias IConstRect2 io.mindspice.mindlib.data.geometry.IConstRect2)
(define-alias IMutRect2 io.mindspice.mindlib.data.geometry.IMutRect2)
(define-alias IAtmoicRect2 io.mindspice.mindlib.data.geometry.IAtomicRect2)
(define-alias IRect2 io.mindspice.mindlib.data.geometry.IRect2)
;; Line
(define-alias IConstLine2 io.mindspice.mindlib.data.geometry.IConstLine2)
(define-alias IMutLine2 io.mindspice.mindlib.data.geometry.IMutLine2)
(define-alias IAtomicLine2 io.mindspice.mindlib.data.geometry.IAtomicLine2)
(define-alias ILine2 io.mindspice.mindlib.data.geometry.ILine2)

;; Other Structures
(define-alias MPair io.mindspice.mindlib.data.tuples.Pair)
(define-alias MTriple io.mindspice.mindlib.data.tuples.Triple)
(define-alias IntList io.mindspice.mindlib.data.collections.lists.primative.IntList)
(define-alias CyclicList io.mindspice.mindlib.data.collections.lists.CyclicList)

#| Enums |#

(define-alias AreaId io.mindspice.outerfieldsserver.enums.AreaId)
(define-alias ClothingItem io.mindspice.outerfieldsserver.enums.ClothingItem)
(define-alias ComponentType io.mindspice.outerfieldsserver.enums.ComponentType)
(define-alias Direction io.mindspice.outerfieldsserver.enums.Direction)
(define-alias EntityState io.mindspice.outerfieldsserver.enums.EntityState)
(define-alias EntityType io.mindspice.outerfieldsserver.enums.EntityType)
(define-alias EventProcMode io.mindspice.outerfieldsserver.enums.EventProcMode)
(define-alias FactionType io.mindspice.outerfieldsserver.enums.FactionType)
(define-alias NavLocation io.mindspice.outerfieldsserver.enums.NavLocation)
(define-alias NavPath io.mindspice.outerfieldsserver.enums.NavPath)
(define-alias PlayerQuests io.mindspice.outerfieldsserver.enums.PlayerQuests)
(define-alias QuestType io.mindspice.outerfieldsserver.enums)
(define-alias SystemType io.mindspice.outerfieldsserver.enums.SystemType)
(define-alias TaskType io.mindspice.outerfieldsserver.enums.TaskType)
(define-alias WorldQuest io.mindspice.outerfieldsserver.enums.WorldQuests)


#| Factories |#

(define-alias ComponentFactory io.mindspice.outerfieldsserver.factory.ComponentFactory)
(define-alias ThoughFactory io.mindspice.outerfieldsserver.factory.ThoughtFactory)

#| Data Wrappers |#

(define-alias ChunkTileIndex io.mindspice.outerfieldsserver.data.wrappers.ChunkTileIndex)
(define-alias DynamicTileRef io.mindspice.outerfieldsserver.data.wrappers.DynamicTileRef)

#| Event & Data |#

(define-alias EventType io.mindspice.outerfieldsserver.systems.event.EventType)
(define-alias Event io.mindspice.outerfieldsserver.systems.event.Event)

#| Java Standard Lib |#

(define-alias ArrayList java.util.ArrayList)
(define-alias HashSet java.util.HashSet)
(define-alias HashMap java.util.HashMap)
(define-alias Enum java.lang.Enum)
(define-alias Arrays java.util.Arrays)

#| Kawa Functional Wrappers |#

;; consumers
(define-alias KConsumer io.mindspice.kawautils.wrappers.functional.consumers.KawaConsumer)
(define-alias KBiConsumer io.mindspice.kawautils.wrappers.functional.consumers.KawaBiConsumer)
(define-alias KTriConsumer io.mindspice.kawautils.wrappers.functional.consumers.KawaTriConsumer)
(define-alias KQuadConsumer io.mindspice.kawautils.wrappers.functional.consumers.KawaQuadConsumer)

;;functions
(define-alias KFunction io.mindspice.kawautils.wrappers.functional.functions.KawaFunction)
(define-alias KBiFunction io.mindspice.kawautils.wrappers.functional.functions.KawaBiFunction)
(define-alias KTriFunction io.mindspice.kawautils.wrappers.functional.functions.KawaTriFunction)
(define-alias KQuadFunction io.mindspice.kawautils.wrappers.functional.functions.KawaQuadFunction)

;; Predicates
(define-alias KPredicate io.mindspice.kawautils.wrappers.functional.predicates.KawaPredicate)
(define-alias KBiPredicate io.mindspice.kawautils.wrappers.functional.predicates.KawaBiPredicate)
(define-alias KTriPredicate io.mindspice.kawautils.wrappers.functional.predicates.KawaTriPredicate)
(define-alias KQuadPredicate io.mindspice.kawautils.wrappers.functional.predicates.KawaQuadPredicate)
;; Suppliers
(define-alias KSupplier io.mindspice.kawautils.wrappers.functional.suppliers.KawaSupplier)


#| Extra MLib Functional |#

(define-alias TriConsumer io.mindspice.mindlib.functional.consumers.TriConsumer)
(define-alias QuadConsumer io.mindspice.mindlib.functional.consumers.QuadConsumer)
(define-alias TriFunction io.mindspice.mindlib.functional.functions.TriFunction)
(define-alias QuadFunction io.mindspice.mindlib.functional.functions.QuadFunction)
(define-alias TriPredicate io.mindspice.mindlib.functional.predicates.TriPredicate)
(define-alias QuadPredicate io.mindspice.mindlib.functional.predicates.QuadPredicate)

(define-alias PredicatedConsumer io.mindspice.mindlib.functional.consumers.PredicatedConsumer)
(define-alias BiPredicatedBiConsumer io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer)





