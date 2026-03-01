package kr.hhplus.be.server.balance.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBalance is a Querydsl query type for Balance
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBalance extends EntityPathBase<Balance> {

    private static final long serialVersionUID = 1574515409L;

    public static final QBalance balance1 = new QBalance("balance1");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final NumberPath<Long> balance = createNumber("balance", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QBalance(String variable) {
        super(Balance.class, forVariable(variable));
    }

    public QBalance(Path<? extends Balance> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBalance(PathMetadata metadata) {
        super(Balance.class, metadata);
    }

}

