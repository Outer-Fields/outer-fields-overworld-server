(define e (emit-event (new-event EventType:.CALLBACK ComponentType:.GLOBAL_POSITION 5  (KConsumer[GlobalPosition]:of (lambda (x) (print  x:mVector:end))))))





(define (to-stream->map->list func lst)
  (((lst:stream):map
    (if (KFunction? func)
      func
      (KFunction func))
  ):toList))
