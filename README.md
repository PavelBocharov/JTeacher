# 📚 JTeacher - Telegram teacher BOT

![start.jpg](src/main/resources/img/start.jpg)

### Work with JSON

1) Build
    ``` sh
   mvn clean install -U
   ```
2) Create JSON files with questions - [EXAMPLE](src/main/resources/java.json). File formate:
    ``` json 
    {
      "type": "/java | /sql | /python - ⚠️required⚠️ types (other in WIP)",
      "description": "OPTIONAL",
      "version": "202510301218",
      "version_description": "YYYY-mm-DD HH:MM -> YYYYmmDDHHMM",
      "questions": [
        {
          "question": "QUESTION",
          "options": [
            "A: A|B|C|D is FIRST word and is ⚠️required⚠️",
            "B: Q2",
            "C: Q3",
            "D: Q4"
          ],
          "correct_answer": "C",
          "detailed_answer": "ANSWER"
        }
      ]
    }
    ```

3) Start `.jar` with params:

| Param                   | Description                                          | Required | Example                                                  |
|-------------------------|:-----------------------------------------------------|:--------:|----------------------------------------------------------|
| `-BT` or `--botToken`   | Bot token from [@BotFather](https://t.me/BotFather). |    ✅     | `123456789:AAAA-abcdabcdabcdabcdabcdabcdabcdabcdabcdabc` |
| `-D` or `--dir`         | Dir path to JSON-files with Q&A.                     |    ✅     | `g/temp/to/gif/files/`                                   |
| `-SI` or `--startImage` | Path to welcome image.                               |    ❌     | `g/temp/to/gif/files/start.png`                          |
| `-BI` or `--baseImage`  | Path to default image.                               |    ❌     | `g/temp/to/gif/files/base.png`                           |

``` sh
java -jar ./target/jteach*.jar --botToken='...' --dir='...'

```

or

``` sh
java -jar ./target/jteach*.jar \
  --botToken='...' \
  --dir='...' \
  --startImage='...' \
  --baseImage='...'
```

### Docker

1) Build image
   ``` sh 
    docker build --no-cache -t marolok/jteach:1.4.0 .
   ```
2) Push image
   ``` sh 
    docker push marolok/jteach:1.4.0
   ```
3) Set ENV in [docker-compose.yml](./docker-compose.yml)
   ``` yml 
   version: '3'
   name: jteach
   services:
   jteach:
     image: marolok/jteach:1.3.0
     container_name: jteach
     environment:
       - BOT_TOKEN=1111111111:222_333333333_444444444444444444-99
       - BOT_POOP_TOKEN=1111111111:222_333333333_444444444444444444-99
       - START_IMG=/opt/app/jteach/img/start.jpg - if in your dir images in `img` folder
       - BASE_IMG=/opt/app/jteach/img/base.jpg - if in your dir images in `img` folder
     volumes:
       - "your/dir/with/json/files/and/imgs:/opt/app/jteach"
   ```
4) 