#!/usr/bin/env python3
"""
PDF Parser Script using PaddleOCR for Knowledge Extraction
This script parses PDF files using PaddleOCR and outputs structured data in JSON format.
"""

import sys
import json
from pathlib import Path
from typing import Dict, Any, List
import os

try:
    from paddleocr import PaddleOCR
    from pdf2image import convert_from_path
    import cv2
    import numpy as np
except ImportError as e:
    print(json.dumps({
        "success": False,
        "error": f"Missing dependencies: {str(e)}. Please run: pip install -r requirements.txt"
    }))
    sys.exit(1)


# Initialize PaddleOCR (run once, use for all pages)
# use_angle_cls=True enables text direction detection
# lang='ch' for Chinese and English mixed, 'en' for English only
ocr = None

def get_ocr_instance():
    """Get or create PaddleOCR instance (singleton pattern)"""
    global ocr
    if ocr is None:
        # Initialize PaddleOCR
        # use_angle_cls: Enable text direction detection
        # lang: 'ch' for Chinese+English, 'en' for English only
        # use_gpu: Set to True if GPU is available
        ocr = PaddleOCR(
            use_angle_cls=True,
            lang='ch',  # Support both Chinese and English
            use_gpu=False,  # Set to True if you have GPU
            show_log=False
        )
    return ocr


def  parse_pdf(pdf_path: str) -> Dict[str, Any]:
    """
    Parse a PDF file using PaddleOCR and extract content.
    
    Args:
        pdf_path: Path to the PDF file
        
    Returns:
        Dictionary containing parsed content and metadata
    """
    try:
        pdf_file = Path(pdf_path)
        
        if not pdf_file.exists():
            raise FileNotFoundError(f"PDF file not found: {pdf_path}")
        
        # Convert PDF to images
        print(f"Converting PDF to images: {pdf_path}", file=sys.stderr)
        images = convert_from_path(pdf_path)
        
        # Get OCR instance
        ocr_engine = get_ocr_instance()
        
        # Process each page
        all_text = []
        page_details = []
        
        for page_num, image in enumerate(images, start=1):
            print(f"Processing page {page_num}/{len(images)}", file=sys.stderr)
            
            # Convert PIL Image to numpy array for PaddleOCR
            img_array = np.array(image)
            
            # Perform OCR
            ocr_result = ocr_engine.ocr(img_array, cls=True)
            
            # Extract text from OCR result
            page_text = []
            page_boxes = []
            
            if ocr_result and ocr_result[0]:
                for line in ocr_result[0]:
                    if line:
                        # line format: [box, (text, confidence)]
                        box = line[0]
                        text_info = line[1]
                        text = text_info[0]
                        confidence = text_info[1]
                        
                        page_text.append(text)
                        page_boxes.append({
                            "text": text,
                            "confidence": float(confidence),
                            "box": [[int(p[0]), int(p[1])] for p in box]
                        })
            
            page_content = "\n".join(page_text)
            all_text.append(page_content)
            
            page_details.append({
                "page": page_num,
                "text": page_content,
                "boxes": page_boxes,
                "text_blocks": len(page_boxes)
            })
        
        # Combine all text
        full_text = "\n\n".join(all_text)
        
        result = {
            "file_name": pdf_file.name,
            "file_path": str(pdf_file.absolute()),
            "content": full_text,
            "metadata": {
                "file_size": pdf_file.stat().st_size,
                "pages": len(images),
                "parser": "PaddleOCR",
                "total_text_blocks": sum(p["text_blocks"] for p in page_details),
                "page_details": page_details
            },
            "success": True,
            "error": None
        }
        
        return result
        
    except Exception as e:
        import traceback
        error_detail = traceback.format_exc()
        print(f"Error: {error_detail}", file=sys.stderr)
        
        return {
            "file_name": Path(pdf_path).name if pdf_path else "unknown",
            "file_path": pdf_path,
            "content": None,
            "metadata": None,
            "success": False,
            "error": str(e)
        }


def main():
    """Main entry point for the script."""
    if len(sys.argv) < 2:
        print(json.dumps({
            "success": False,
            "error": "Usage: python parse_pdf.py <pdf_file_path>"
        }))
        sys.exit(1)
    
    pdf_path = sys.argv[1]
    result = parse_pdf(pdf_path)
    
    # Output result as JSON
    print(json.dumps(result, ensure_ascii=False, indent=2))
    
    # Exit with appropriate code
    sys.exit(0 if result["success"] else 1)


if __name__ == "__main__":
    main()
