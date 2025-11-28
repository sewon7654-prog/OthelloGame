# 픽셀 폰트 적용 가이드

## 1. 픽셀 폰트 파일 다운로드

### 추천 한글 픽셀 폰트:
1. **Neo둥근모** (NeoDunggeunmo)
   - 다운로드: https://github.com/Dalgona/neodgm
   - 또는: https://fonts.google.com/noto/specimen/Noto+Sans+KR (일반 폰트지만 픽셀 느낌 가능)

2. **DungGeunMo** (둥근모)
   - 다운로드: https://www.fontspace.com/dunggeunmo-font-f25661

3. **Press Start 2P** (영문 전용, 한글은 지원 안 함)
   - 다운로드: https://fonts.google.com/specimen/Press+Start+2P

### 무료 한글 픽셀 폰트 사이트:
- https://www.fontspace.com (검색: "korean pixel")
- https://www.dafont.com (검색: "korean pixel")
- https://github.com (검색: "korean pixel font")

## 2. 폰트 파일을 프로젝트에 추가

1. `src/main/resources/fonts/` 폴더 생성
2. 다운로드한 `.ttf` 파일을 해당 폴더에 복사
   - 예: `src/main/resources/fonts/NeoDunggeunmo.ttf`

## 3. Java 코드에서 폰트 로드

```java
// FontService.java 생성 예시
public class FontService {
    private static Font pixelFont;
    
    static {
        try {
            pixelFont = Font.loadFont(
                FontService.class.getResourceAsStream("/fonts/NeoDunggeunmo.ttf"),
                16
            );
        } catch (Exception e) {
            // 폰트 로드 실패 시 기본 폰트 사용
            pixelFont = Font.font("Courier New", 16);
        }
    }
    
    public static Font getPixelFont(double size) {
        return Font.font(pixelFont.getFamily(), size);
    }
}
```

## 4. CSS에서 폰트 사용

```css
.root {
    -fx-font-family: "NeoDunggeunmo", "Courier New", monospace;
}
```

## 5. Java 코드에서 직접 적용

```java
Label label = new Label("오셀로 게임");
label.setFont(FontService.getPixelFont(72));
```

