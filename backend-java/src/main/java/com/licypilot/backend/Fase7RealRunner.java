package com.licypilot.backend;

import com.licypilot.backend.model.*;
import com.licypilot.backend.repository.AnaliseUsuarioRepository;
import com.licypilot.backend.repository.EmpresaRepository;
import com.licypilot.backend.repository.LicitacaoRepository;
import com.licypilot.backend.service.DiagnosticoMatchService;
import com.licypilot.backend.service.LicitacaoService;
import com.licypilot.backend.service.ViabilidadeService;
import com.licypilot.backend.util.LogPadrao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@Component
@Profile("real")
public class Fase7RealRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    private static class CustomMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String name;
        public CustomMultipartFile(byte[] content, String name) { this.content = content; this.name = name; }
        @Override public String getName() { return "arquivo"; }
        @Override public String getOriginalFilename() { return name; }
        @Override public String getContentType() { return "application/pdf"; }
        @Override public boolean isEmpty() { return content.length == 0; }
        @Override public long getSize() { return content.length; }
        @Override public byte[] getBytes() { return content; }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(content); }
        @Override public void transferTo(File dest) { }
    }

    private static final Logger log = LoggerFactory.getLogger(Fase7RealRunner.class);
    
    private final EmpresaRepository empresaRepository;
    private final LicitacaoRepository licitacaoRepository;
    private final LicitacaoService licitacaoService;
    private final ViabilidadeService viabilidadeService;
    private final DiagnosticoMatchService diagnosticoMatchService;
    private final AnaliseUsuarioRepository analiseUsuarioRepository;

    public Fase7RealRunner(EmpresaRepository empresaRepository, 
                           LicitacaoRepository licitacaoRepository,
                           LicitacaoService licitacaoService,
                           ViabilidadeService viabilidadeService, 
                           DiagnosticoMatchService diagnosticoMatchService, 
                           AnaliseUsuarioRepository analiseUsuarioRepository,
                           JdbcTemplate jdbcTemplate) {
        this.empresaRepository = empresaRepository;
        this.licitacaoRepository = licitacaoRepository;
        this.licitacaoService = licitacaoService;
        this.viabilidadeService = viabilidadeService;
        this.diagnosticoMatchService = diagnosticoMatchService;
        this.analiseUsuarioRepository = analiseUsuarioRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info(">>>> INICIANDO FASE 7: PROCESSAMENTO REAL <<<<");

        // Ajuste de banco para garantir bytea (executa apenas uma vez)
        try {
            jdbcTemplate.execute("ALTER TABLE licitacoes RENAME COLUMN arquivo_conteudo TO arquivo_conteudo_old");
            jdbcTemplate.execute("ALTER TABLE licitacoes ADD COLUMN arquivo_conteudo bytea");
            log.info("Ajuste de esquema aplicado: coluna arquivo_conteudo convertida para bytea.");
        } catch (Exception e) {
            log.info("evento={} origem={} resumo={}", LogPadrao.EVENTO_ESQUEMA_NAO_APLICADO, "Fase7RealRunner.run", e.getMessage());
        }

        Empresa empresaAlta = criarEmpresa("ALTA - LicyTech TI", "11.111.111/0001-11", 1000000.0, "EPP", List.of("6201-5/00"));
        
        File editalFile = new File("..\\EditalLicitaçãoTeste\\EDITAL20263.pdf");
        if (!editalFile.exists()) {
            LogPadrao.logErro(log, LogPadrao.EVENTO_ARQUIVO_NAO_ENCONTRADO, "Fase7RealRunner.run", "caminho", editalFile.getAbsolutePath(), "Arquivo de edital não encontrado");
            return;
        }

        byte[] content = Files.readAllBytes(editalFile.toPath());
        CustomMultipartFile customFile = new CustomMultipartFile(content, editalFile.getName());

        // Agora usamos o 'true' para forçar o reprocessamento e testar o novo código
        log.info("Importando edital com 'reprocessar=true' para teste...");
        Licitacao licitacao = licitacaoService.importarLicitacao(customFile, "Edital de Teste", "Órgão Demo", 10, true);
        
        log.info("Aguardando processamento sequencial...");
        int tentativas = 0;
        UUID licitacaoId = licitacao.getId();
        while (tentativas < 120) {
            licitacao = licitacaoRepository.findById(licitacaoId).orElse(licitacao);
            if (licitacao.getStatusProcessamento() == StatusProcessamento.CONCLUIDO) break;
            Thread.sleep(5000);
            if (tentativas % 6 == 0) log.info("Status atual: {}...", licitacao.getStatusProcessamento());
            tentativas++;
        }

        if (licitacao.getStatusProcessamento() == StatusProcessamento.CONCLUIDO) {
            List<AnaliseUsuario> analises = analiseUsuarioRepository.findAll().stream()
                    .filter(a -> a.getLicitacao().getId().equals(licitacaoId))
                    .filter(a -> a.getEmpresa().getId().equals(empresaAlta.getId()))
                    .toList();

            for (AnaliseUsuario analise : analises) {
                log.info("Gerando Veredito Final Humanizado para TI...");
                AnaliseUsuario resultado = diagnosticoMatchService.executarDiagnosticoCompleto(analise.getId());
                log.info("VEREDITO DO CONSULTOR:\n{}", resultado.getDiagnosticoJson().path("veredito_do_especialista").asText());
            }
        }
        log.info(">>>> FIM DA FASE 7 <<<<");
    }

    private Empresa criarEmpresa(String nome, String cnpj, Double capital, String porte, List<String> cnaes) {
        return empresaRepository.findByCnpj(cnpj).orElseGet(() -> {
            Empresa e = new Empresa();
            e.setRazaoSocial(nome);
            e.setCnpj(cnpj);
            e.setCapitalSocial(capital);
            e.setPorte(porte);
            e.setCnaes(cnaes);
            return empresaRepository.save(e);
        });
    }
}
