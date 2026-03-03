package kr.hhplus.be.server.payment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPayment is a Querydsl query type for Payment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayment extends EntityPathBase<Payment> {

    private static final long serialVersionUID = -882296431L;

    public static final QPayment payment = new QPayment("payment");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final NumberPath<Long> amount = createNumber("amount", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final NumberPath<Long> reservationId = createNumber("reservationId", Long.class);

    public final EnumPath<PaymentStatus> status = createEnum("status", PaymentStatus.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QPayment(String variable) {
        super(Payment.class, forVariable(variable));
    }

    public QPayment(Path<? extends Payment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPayment(PathMetadata metadata) {
        super(Payment.class, metadata);
    }

}

