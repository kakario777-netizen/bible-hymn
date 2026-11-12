# 성경찬송 (bible-hymn)

안드로이드용 성경·찬송가 앱 (개인용).

- **성경**: 개역한글 66권 — 출처: [thiagobodruk/bible](https://github.com/thiagobodruk/bible) `ko_krv.json`
- **찬송가**: 통일찬송가 558장 악보 (`app/src/main/assets/hymn/score`), 제목 목록 `hymn/titles.txt`

- **내 영의 노래**: 417장 악보 (`app/src/main/assets/mysoul`)

main 브랜치에 올리면 GitHub Actions가 APK 두 개를 만들어 Releases의 `latest`에 올린다.

- `bible-hymn.apk` — 폰용 (`com.kakario.biblehymn`)
- `bible-hymn-eink.apk` — 이북리더기(sam 10 Plus)용 (`com.kakario.biblehymn.eink`, 앱 이름 '성경찬송 이북')

기능은 같고, 이북 전용 조절은 코드에서 `BuildConfig.IS_EINK`로 갈라서 이북판에만 적용한다.
