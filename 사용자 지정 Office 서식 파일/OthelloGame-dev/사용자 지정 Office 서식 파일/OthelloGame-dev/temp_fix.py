import re, pathlib
path = pathlib.Path('src/main/java/org/example/ui/GameView.java')
text = path.read_text(encoding='utf-8')
pattern = re.compile(r"if \(cardUsed\[cardIndex\]\) \{.*?return;\s*\}", re.S)
replacement = """if (cardUsed[cardIndex]) {
            showAlert(\"카드 사용 불가\", \"이미 사용한 카드입니다.\");
            return;
        }"""
new_text, count = pattern.subn(replacement, text, 1)
if count != 1:
    raise SystemExit(f'replaced {count}')
path.write_text(new_text, encoding='utf-8')
