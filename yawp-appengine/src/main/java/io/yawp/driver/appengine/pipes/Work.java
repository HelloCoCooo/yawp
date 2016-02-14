package io.yawp.driver.appengine.pipes;

import io.yawp.commons.utils.ReflectionUtils;
import io.yawp.repository.IdRef;
import io.yawp.repository.annotations.Endpoint;
import io.yawp.repository.annotations.Id;
import io.yawp.repository.annotations.Index;
import io.yawp.repository.annotations.Json;
import io.yawp.repository.pipes.Pipe;
import io.yawp.repository.pipes.SinkMarker;
import io.yawp.repository.pipes.SourceMarker;

import java.util.logging.Logger;

import static io.yawp.repository.Yawp.yawp;

@Endpoint(kind = "__yawp_pipe_works")
public class Work {

    private final static Logger logger = Logger.getLogger(Work.class.getName());

    @Id
    private IdRef<Work> id;

    @Index(normalize = false)
    private String indexHash;

    @Json
    private Payload payload;

    public Work() {
    }

    public Work(String indexHash, Payload payload) {
        this.indexHash = indexHash;
        this.payload = payload;
    }

    public <T, S> void execute(Object sink, SinkMarker sinkMarker) {
        Pipe<T, S> pipe = createPipeInstance();

        if (sinkMarker.isPresent()) {
            logger.info(String.format("REFLUX %s (present=%b souceVersion=%d, sinkVersion=%d)", sinkMarker.getParentId(), payload.isPresent(), payload.getSourceMarker().getVersion(), sinkMarker.getVersion()));
            pipe.reflux((T) sinkMarker.getSource(), (S) sink);
        }

        if (payload.isPresent()) {
            logger.info(String.format("FLUX %s (souceVersion=%d, sinkVersion=%d)", sinkMarker.getParentId(), payload.getSourceMarker().getVersion(), sinkMarker.getVersion()));
            pipe.flux((T) payload.getSource(), (S) sink);
            rememberSourceInSinkMarker(sinkMarker);
        }

        sinkMarker.setPresent(payload.isPresent());
        sinkMarker.setVersion(payload.getSourceMarker().getVersion());

//        //
//
//
//        if (payload.isPresent()) {
//            if (sinkMarker.isPresent()) {
//                logger.info(String.format("PRESENT REFLUX %s (souceVersion=%d, sinkVersion=%d)", sinkMarker.getParentId(), payload.getSourceMarker().getVersion(), sinkMarker.getVersion()));
//                pipe.reflux((T) sinkMarker.getSource(), (S) sink);
//            }
//            logger.info(String.format("PRESENT FLUX %s (souceVersion=%d, sinkVersion=%d)", sinkMarker.getParentId(), payload.getSourceMarker().getVersion(), sinkMarker.getVersion()));
//            pipe.flux((T) payload.getSource(), (S) sink);
//            rememberSourceInSinkMarker(sinkMarker);
//        } else {
//            logger.info(String.format("NOT PRESENT reflux %s (souceVersion=%d, sinkVersion=%d)", sinkMarker.getParentId(), payload.getSourceMarker().getVersion(), sinkMarker.getVersion()));
//            pipe.reflux((T) payload.getSource(), (S) sink);
//        }
//
//        sinkMarker.setVersion(payload.getSourceMarker().getVersion());
//        sinkMarker.setPresent(payload.isPresent());
    }

    private void rememberSourceInSinkMarker(SinkMarker sinkMarker) {
        sinkMarker.setSourceJson(ReflectionUtils.getFeatureEndpointClazz(payload.getPipeClazz()), payload.getSourceJson());
    }

    private <T, S> Pipe<T, S> createPipeInstance() {
        try {
            return payload.getPipeClazz().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public IdRef<SinkMarker> createSinkMarkerId() {
        IdRef<?> sourceId = payload.getSourceId();
        IdRef<?> sinkId = payload.getSinkId();

        IdRef<SinkMarker> sinkMarkerId;
        if (sourceId.getId() != null) {
            sinkMarkerId = IdRef.create(yawp(), SinkMarker.class, sourceId.getId());
            sinkMarkerId.setParentId(sinkId.createChildId(sourceId.getClazz(), sourceId.getId()));
        } else {
            sinkMarkerId = IdRef.create(yawp(), SinkMarker.class, sourceId.getName());
            sinkMarkerId.setParentId(sinkId.createChildId(sourceId.getClazz(), sourceId.getName()));
        }

        return sinkMarkerId;
    }

    public SourceMarker getSourceMarker() {
        return payload.getSourceMarker();
    }

    public Long getSourceVersion() {
        return payload.getSourceMarker().getVersion();
    }

    public IdRef<Work> getId() {
        return id;
    }
}