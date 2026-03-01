package kr.hhplus.be.server.concert.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QConcertDetail is a Querydsl query type for ConcertDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QConcertDetail extends EntityPathBase<ConcertDetail> {

    private static final long serialVersionUID = 1548859714L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QConcertDetail concertDetail = new QConcertDetail("concertDetail");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final QConcert concert;

    public final DatePath<java.time.LocalDate> concertDate = createDate("concertDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public QConcertDetail(String variable) {
        this(ConcertDetail.class, forVariable(variable), INITS);
    }

    public QConcertDetail(Path<? extends ConcertDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QConcertDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QConcertDetail(PathMetadata metadata, PathInits inits) {
        this(ConcertDetail.class, metadata, inits);
    }

    public QConcertDetail(Class<? extends ConcertDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.concert = inits.isInitialized("concert") ? new QConcert(forProperty("concert")) : null;
    }

}

