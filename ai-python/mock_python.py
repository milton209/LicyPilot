from fastapi import FastAPI, UploadFile, File
from pydantic import BaseModel
from typing import List, Optional
import os
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="LicyPilot - Mock Python Extractor")

class SectionSegment(BaseModel):
    pagina_inicio: int
    pagina_fim: int
    texto_limpo: str
    secao_sugerida: Optional[str] = None

class ExtractionResponse(BaseModel):
    filename: str
    segments: List[SectionSegment]
    is_ocr: bool = False

@app.post("/extract", response_model=ExtractionResponse)
async def extract_mock(file: UploadFile = File(...)):
    """
    Mock endpoint que ignora o PDF e retorna o conteúdo do JSONmaster.txt
    """
    logger.info(f"Recebida requisição para extração (Mock): {file.filename}")
    
    # Caminho do arquivo JSON pronto
    mock_file_path = os.path.join("..", "EditalLicitaçãoTeste", "JSONmaster.txt")
    
    try:
        with open(mock_file_path, "r", encoding="utf-8") as f:
            content = f.read()
            
        logger.info("Retornando conteúdo do JSONmaster.txt como texto extraído.")
        
        # Simulamos que o conteúdo inteiro veio em um único "bloco"
        return ExtractionResponse(
            filename=file.filename,
            segments=[
                SectionSegment(
                    pagina_inicio=1,
                    pagina_fim=1,
                    texto_limpo=content
                )
            ],
            is_ocr=False
        )
    except Exception as e:
        logger.error(f"Erro ao ler mock: {str(e)}")
        return ExtractionResponse(
            filename=file.filename,
            segments=[SectionSegment(pagina_inicio=0, pagina_fim=0, texto_limpo=f"ERRO: {str(e)}")],
            is_ocr=False
        )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001) # Porta 8001 para não conflitar se o original estiver rodando
