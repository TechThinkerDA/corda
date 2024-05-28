package net.corda.samples.example.states

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.schemas.QueryableState
import net.corda.core.contracts.BelongsToContract
import net.corda.core.schemas.MappedSchema
import net.corda.samples.example.contracts.SilverBarContract
import net.corda.samples.example.schema.SilverBarSchemaV1

@BelongsToContract(SilverBarContract::class)
data class SilverBarState(
        val weight: Double,
        val owner: Party,
        val issuer: Party,
        val linearId: UniqueIdentifier = UniqueIdentifier()
) : ContractState, QueryableState {

    override val participants: List<Party> get() = listOf(owner, issuer)

    override fun generateMappedObject(schema: MappedSchema) = SilverBarSchemaV1.PersistentSilverBar(
            this.owner.name.toString(),
            this.issuer.name.toString(),
            this.weight,
            this.linearId.id
    )

    override fun supportedSchemas() = listOf(SilverBarSchemaV1)
}
