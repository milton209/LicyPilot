package com.licypilot.backend.service;

import com.licypilot.backend.dto.MasterJsonRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MasterJsonMerger {

    public MasterJsonRecord merge(MasterJsonRecord original, MasterJsonRecord partial) {
        if (original == null) return partial;
        if (partial == null) return original;

        return new MasterJsonRecord(
            mergeIdentificacao(original.identificacaoProjeto(), partial.identificacaoProjeto()),
            mergePrazos(original.prazosValoresPagamento(), partial.prazosValoresPagamento()),
            mergeLogistica(original.logisticaAmostras(), partial.logisticaAmostras()),
            mergeList(original.habilitacaoDetalhada(), partial.habilitacaoDetalhada()),
            mergeList(original.qualificacaoTecnicaEspecifica(), partial.qualificacaoTecnicaEspecifica()),
            mergeRegras(original.regrasDisputa(), partial.regrasDisputa()),
            mergeList(original.analiseRiscoPenalidades(), partial.analiseRiscoPenalidades())
        );
    }

    private <T> List<T> mergeList(List<T> original, List<T> partial) {
        List<T> merged = new ArrayList<>();
        if (original != null) merged.addAll(original);
        if (partial != null) merged.addAll(partial);
        return merged;
    }

    private MasterJsonRecord.IdentificacaoProjetoRecord mergeIdentificacao(MasterJsonRecord.IdentificacaoProjetoRecord o, MasterJsonRecord.IdentificacaoProjetoRecord p) {
        if (o == null) return p;
        if (p == null) return o;
        return new MasterJsonRecord.IdentificacaoProjetoRecord(
            p.processo_licitatorio() != null ? p.processo_licitatorio() : o.processo_licitatorio(),
            p.numero_edital() != null ? p.numero_edital() : o.numero_edital(),
            p.orgao_emissor() != null ? p.orgao_emissor() : o.orgao_emissor(),
            p.modalidade() != null ? p.modalidade() : o.modalidade(),
            p.criterio_julgamento() != null ? p.criterio_julgamento() : o.criterio_julgamento(),
            p.objeto_completo() != null ? p.objeto_completo() : o.objeto_completo()
        );
    }

    private MasterJsonRecord.PrazosPagamentoRecord mergePrazos(MasterJsonRecord.PrazosPagamentoRecord o, MasterJsonRecord.PrazosPagamentoRecord p) {
        if (o == null) return p;
        if (p == null) return o;
        return new MasterJsonRecord.PrazosPagamentoRecord(
            p.data_abertura_sessao() != null ? p.data_abertura_sessao() : o.data_abertura_sessao(),
            p.valor_estimado_total() != null ? p.valor_estimado_total() : o.valor_estimado_total(),
            p.prazo_execucao_global_dias() != null ? p.prazo_execucao_global_dias() : o.prazo_execucao_global_dias(),
            p.prazo_para_pagamento_dias() != null ? p.prazo_para_pagamento_dias() : o.prazo_para_pagamento_dias(),
            p.condicoes_pagamento_detalhes() != null ? p.condicoes_pagamento_detalhes() : o.condicoes_pagamento_detalhes()
        );
    }

    private MasterJsonRecord.LogisticaAmostrasRecord mergeLogistica(MasterJsonRecord.LogisticaAmostrasRecord o, MasterJsonRecord.LogisticaAmostrasRecord p) {
        if (o == null) return p;
        if (p == null) return o;
        return new MasterJsonRecord.LogisticaAmostrasRecord(
            p.locais_de_entrega() != null ? p.locais_de_entrega() : o.locais_de_entrega(),
            p.prazo_entrega_dias() != null ? p.prazo_entrega_dias() : o.prazo_entrega_dias(),
            p.exige_amostra_fisica() != null ? p.exige_amostra_fisica() : o.exige_amostra_fisica(),
            p.detalhes_amostra_ou_teste() != null ? p.detalhes_amostra_ou_teste() : o.detalhes_amostra_ou_teste()
        );
    }

    private MasterJsonRecord.RegrasDisputaRecord mergeRegras(MasterJsonRecord.RegrasDisputaRecord o, MasterJsonRecord.RegrasDisputaRecord p) {
        if (o == null) return p;
        if (p == null) return o;
        return new MasterJsonRecord.RegrasDisputaRecord(
            p.modo_disputa() != null ? p.modo_disputa() : o.modo_disputa(),
            p.beneficio_me_epp() != null ? p.beneficio_me_epp() : o.beneficio_me_epp(),
            p.criterios_desempate() != null ? p.criterios_desempate() : o.criterios_desempate()
        );
    }
}
