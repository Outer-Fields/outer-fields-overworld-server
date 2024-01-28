
#| General |#


(define (print-list lst)
  (let ((result ""))
    (for-each (lambda (value)
                (set! result (string-append result (value:toString) "\\n")))
              lst)
    result))

(define (print-enum-values enum)
  (let ((result ""))
    (for-each (lambda (value)
                (set! result (string-append result (value:toString) "\\n")))
              (enum:values))
    result))

(define (string-to-enum str ::String enum-class)
  (let ((enum-value (java.lang.Enum:valueOf enum-class (string-upcase str))))
      enum-value))


#| Systems |#
(define (get-system-listener systemType)
  (EntityManager:systemListenerByType (systemType)))

(define (get-all-system-listeners)
  (EntityManager:systemListeners))


#| Entity |#

(define (get-all-entities)
  (EntityManager:allEntities))

(define (get-entity-by-id id )
  (EntityManager:entityById id))


#| Events |#

(define (emit-event event)
  (EntityManager:emitEvent event) #t)

(define (new-event eventType componentType  entityId  data)
  (Event:fromShell (eventType) (componentType) entityId data))

(define (new-area-event eventType componentType  areaId  entityId  data)
  (Event:fromShell (eventType)  (componentType) (areaId) entityId data))


#| Event Monitoring |#

(define (monitor-event eventType predicate)
  (EntityManager:addMonitoredEvent
    (if (KPredicate? predicate)
      ((eventType) predicate)
      ((eventType) (KPredicate predicate)))))

(define (un-monitor-event eventType )
  (EntityManager:removeMonitoredEvent (eventType)))

(define (clear-monitored-events )
  (EntityManager:clearMonitoredEvents))

(define (get-monitored-events)
  (EntityManager:getMonitoredEvents))

(define (monitor-entity-event event-type entity-id)
  (monitor-event event-type
    (KPredicate[Event]:of
      (lambda (x ::Event)
        (= (x:issuerEntityId) entity-id)))))

#| Streams |#

(define (to-stream->filter->list filter lst)
  (((lst:stream):filter
    (if (KPredicate? filter)
      filter
      (KPredicate filter))
  ):toList))


(define (to-stream->map->list func lst)
  (((lst:stream):map
    (if (KFunction? func)
      func
      (KFunction func))
  ):toList))

(define (to-stream->filter->map->list filter map lst)
  ((((lst:stream):filter
    (if (KPredicate? filter) (filter) (KPredicate filter))):map
    (if (KFunction? map) (map) (KFunction map))):toList))

(define (stream->map func)
  (lambda (x)
    (if (KFunction? func)
      (x:map func)
      (x:map (KFunction func)))))

(define (stream->filter func)
  (lambda (x)
    (if (KPredicate? func)
      (x:filter func)
      (x:filter (KPredicate func)))))

(define (stream->find-any func)
  (lambda (x)
    (if (KPredicate? func)
      (x:findAny func)
      (x:findAny (KPredicate func)))))

(define (stream->none-match func)
  (lambda (x)
    (if (KPredicate? func)
      (x:noneMatch func)
      (x:noneMatch (KPredicate func)))))

(define (stream->any-match func)
  (lambda (x)
    (if (KPredicate? func)
      (x:anyMatch func)
      (x:anyMatch (KPredicate func)))))

(define (stream->all-match func)
  (lambda (x)
    (if (KPredicate? func)
      (x:allMatch func)
      (x:allMatch (KPredicate func)))))

(define (stream->find-first func)
  (lambda (x)
    (if (KPredicate? func)
      (x:findFirst func)
      (x:findAny (KPredicate func)))))

(define (stream->limit limit)
  (lambda (x) (x:limit limit)))

(define (stream->sorted)
  (lambda (x) (x:sorted)))

(define (stream->list)
  (lambda (x) (x:toList)))

(define (stream->distinct)
  (lambda (x) (x:distinct)))

(define (list->stream)
  (lambda (x) (x:stream)))

(define (compose-stream func-list lst)
  (if (null? (cdr func-list))
    ((car func-list) lst)
    (compose-stream (cdr func-list) ((car func-list) lst))))
