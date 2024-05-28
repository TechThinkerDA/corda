package net.corda.samples.example.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object SilverBarSchema

object SilverBarSchemaV1 : MappedSchema(
        schemaFamily = SilverBarSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentSilverBar::class.java)
) {
    @Entity
    @Table(name = "silver_bars")
    class PersistentSilverBar(
            @Column(name = "owner")
            var owner: String,

            @Column(name = "issuer")
            var issuer: String,

            @Column(name = "weight")
            var weight: Double,

            @Column(name = "linear_id")
            @Type(type = "uuid-char")
            var linearId: UUID
    ) : PersistentState() {
        constructor() : this("","", 0.0, UUID.randomUUID()) {

        }
    }
}
