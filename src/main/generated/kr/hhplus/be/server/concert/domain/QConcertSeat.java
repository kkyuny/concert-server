package kr.hhplus.be.server.concert.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QConcertSeat is a Querydsl query type for ConcertSeat
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QConcertSeat extends EntityPathBase<ConcertSeat> {

    private static final long serialVersionUID = 448984886L;

    public static final QConcertSeat concertSeat = new QConcertSeat("concertSeat");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final NumberPath<Long> concertDetailId = createNumber("concertDetailId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final NumberPath<Integer> seatNo = createNumber("seatNo", Integer.class);

    public final EnumPath<SeatStatus> status = createEnum("status", SeatStatus.class);

    public QConcertSeat(String variable) {
        super(ConcertSeat.class, forVariable(variable));
    }

    public QConcertSeat(Path<? extends ConcertSeat> path) {
        super(path.getType(), path.getMetadata());
    }

    public QConcertSeat(PathMetadata metadata) {
        super(ConcertSeat.class, metadata);
    }

}

