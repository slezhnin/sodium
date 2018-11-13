## @fa[thumbs-up] Быстрый доступ к медленным данным​

Или как заставить ленивых  котиков  быстро бегать​

---
## @fa[compass] Тезисы​

@ul

- Формат и способ хранения котиков​
- Медиатор для котиков​
- Доступ к котикам​
- Живая демонстрация котиков

@ulend

---
## @fa[file] JSON, HOCON, YAML?​

@ul

- Какой формат выбрать для хранения?
- Да какой вам удобно.

<br>

- Но мы будем испольовать JSON.

@ulenf

---?code=sodium-store/src/main/run/sodium-store.json&lang=json&title=@fa[file-code-o] JSON Configuration File

---?image=assets/repo_qr_code.png&position=bottom 1px right 1px&size=479px 479px
## @fa[question] Мёрдж, комит, бранч, ревёрт

@ul

- Что это, Бэрримор?​

<br>

- Это Git, сэр!​
- QR CODE для репозитория @fa[long-arrow-right]

@ulend

---
## @fa[code-fork] Git – это мягкий и пушистый котик​

@ul

- Распределённый​
- Синхронизируемый​
- Многопользовательский​
- Удобный​
- Безопасный​

​<br>

- Мы будем использовать его для хранения данных​

@ulend

---
## Общая схема

![General Schema](assets/general_schema.png)

---
## Пара слов об Eclipse Vert.x

@ul

- Это набор инструментов для построения реактивных приложений
- Неблокируюший подход к архитектуре позволяет эффективно масштабироватся
- Поддерживает множество языков программирования @note[You can use Vert.x with multiple languages including Java, JavaScript, Groovy, Ruby, Ceylon, Scala and Kotlin.]

<br>

- Подробнее на сайте [vertx.io](https://vertx.io/)

@ulend
