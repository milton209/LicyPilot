from fastapi import FastAPI, UploadFile, File, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import pdfplumber
import io
import os
import logging
from pdf2image import convert_from_bytes
import pytesseract
from PIL import Image

# Configuração de Logs
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Opcional: Caminho para o executável do tesseract no Windows
# Se o Tesseract não estiver no PATH, descomente e ajuste a linha abaixo:
# pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

app = FastAPI(title="LicyPilot - AI Python Extractor")

class SectionSegment(BaseModel):
    pagina_inicio: int
    pagina_fim: int
    texto_limpo: str
    secao_sugerida: Optional[str] = None

class ExtractionResponse(BaseModel):
    filename: str
    segments: List[SectionSegment]
    is_ocr: bool = False

def run_ocr_on_page(page_bytes: bytes, page_number: int) -> str:
    """
    Converte página do PDF em imagem e executa OCR.
    """
    try:
        # Converte apenas a página específica do buffer de bytes
        images = convert_from_bytes(page_bytes, first_page=page_number, last_page=page_number)
        if not images:
            return ""
        
        # Executa OCR com pytesseract (linguagem português + inglês)
        text = pytesseract.image_to_string(images[0], lang='por+eng')
        return text
    except Exception as e:
        logger.error(f"Erro ao executar OCR na página {page_number}: {str(e)}")
        return f"[ERRO OCR PÁGINA {page_number}]"

def clean_text(text: str) -> str:
    """
    Limpa o texto extraído removendo ruídos comuns, 
    cabeçalhos e rodapés repetitivos (simplificado).
    """
    if not text:
        return ""
    
    lines = text.splitlines()
    cleaned_lines = []
    
    for line in lines:
        stripped = line.strip()
        # Ignora linhas vazias ou muito curtas que parecem ruído de rodapé/página
        if len(stripped) < 3:
            continue
        cleaned_lines.append(stripped)
    
    return "\n".join(cleaned_lines)

@app.post("/extract", response_model=ExtractionResponse)
async def extract_pdf(file: UploadFile = File(...)):
    """
    Endpoint principal para extrair texto de PDFs de licitação.
    """
    if not file.filename.endswith('.pdf'):
        raise HTTPException(status_code=400, detail="Apenas arquivos PDF são suportados.")
    
    content = await file.read()
    segments = []
    is_ocr_active = False
    
    try:
        with pdfplumber.open(io.BytesIO(content)) as pdf:
            logger.info(f"Processando PDF: {file.filename} com {len(pdf.pages)} páginas.")
            
            for i, page in enumerate(pdf.pages):
                text = page.extract_text()
                
                # Regra 7A.2: Se o texto extraído for muito curto, identifica como imagem (OCR necessário)
                if not text or len(text.strip()) < 50:
                    logger.info(f"Página {i+1} parece ser imagem. Acionando OCR fallback.")
                    
                    # Passa o número da página (i+1) para o OCR
                    text_ocr = run_ocr_on_page(content, i + 1)
                    
                    if text_ocr and len(text_ocr.strip()) > 10:
                        text = text_ocr
                        is_ocr_active = True
                    else:
                        text = "[CONTEÚDO ILEGÍVEL OU VAZIO]"
                
                cleaned = clean_text(text)
                
                segments.append(SectionSegment(
                    pagina_inicio=i + 1,
                    pagina_fim=i + 1,
                    texto_limpo=cleaned
                ))
                
        return ExtractionResponse(
            filename=file.filename,
            segments=segments,
            is_ocr=is_ocr_active
        )
        
    except Exception as e:
        logger.error(f"Erro na extração: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Erro ao processar PDF: {str(e)}")

@app.get("/health")
def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
