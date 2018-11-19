---?image=assets/bg/lazy-cats-leaves-animals-opt.jpg
@transition[none]

@snap[north]
@box[bg-green text-white waved](@fa[space-shuttle] Быстрый доступ к медленным данным​#Или как заставить ленивых  котиков  быстро бегать​)
@snapend

@snap[east]
![Profile Photo](assets/profile_photo_2.jpg)
@snapend

@snap[west]
@ul[](false)

- Сергей Лежнин
- Сбербанк Технологии
- [s.lezhnin@gmail.com](mailto:s.lezhnin@gmail.com)

@ulend
@snapend

@snap[south]
[Vert.x](https://vertx.io) @fa[chevron-left] [Kotlin](https://kotlinlang.org)
@snapend

---?image=assets/bg/lazy-cats-leaves-animals-opt.jpg
### @fa[compass] Тезисы​

@ul

- Формат и способ хранения котиков​
- Медиатор для котиков​
- Доступ к котикам​
- Живая демонстрация котиков

@ulend

---?image=assets/bg/lazy-cats-leaves-animals-opt.jpg
### @fa[file] JSON, HOCON, YAML?​

@ul

- Какой формат выбрать для хранения?
- Да какой вам удобно.

<br>

- Но мы будем испольовать JSON.

@ulenf

---?code=sodium-store/src/main/run/sodium-store.json&lang=json&title=@fa[file-code-o] JSON Configuration File

---?image=assets/bg/lazy-cats-leaves-animals-opt.jpg
@transition[none]

@snap[north]
#### @fa[question] Мёрдж, комит, бранч, ревёрт
@snapend

@snap[south-west]
@quote[Что это, Бэрримор?](​Это Git, сэр!)​

@ul

- QR CODE для репозитория @fa[long-arrow-right]

<br>

- https://github.com/slezhnin/sodium

@ulend
@snapend

@snap[east]
![QR](assets/logo_qr.png)
@snapend

---?image=assets/bg/lazy-cats-leaves-animals-opt.jpg
### @fa[code-fork] Git – это мягкий и пушистый котик​

@ul

- Распределённый​
- Многопользовательский​
- Удобный​
- Безопасный​

​<br>

- Мы будем использовать его для хранения данных​

@ulend

---?image=assets/bg/lazy-cats-leaves-animals-opt.jpg
### Пара слов об Eclipse Vert.x

@ul

- Это набор инструментов для построения реактивных приложений
- Неблокируюший подход к архитектуре позволяет эффективно масштабироватся
- Поддерживает множество языков программирования @note[You can use Vert.x with multiple languages including Java, JavaScript, Groovy, Ruby, Ceylon, Scala and Kotlin.]

<br>

- Подробнее на сайте [vertx.io](https://vertx.io/)

@ulend

---?image=assets/bg/lazy-cats-leaves-animals-opt.jpg
### Общая схема

![General Schema](assets/general_schema.png)

---?code=sodium-store/src/main/kotlin/com/lezhnin/project/sodium/store/manager/ManagerVerticle.kt&lang=kotlin&title=Manager Verticle

@[9-21](Инициализация маршрутизации)
@[23-29](Инициализация HTTP сервера)

+++?code=sodium-store/src/main/kotlin/com/lezhnin/project/sodium/store/manager/ManagerDataRequestHandler.kt&lang=kotlin&title=Manager Data Request Handler

@[10-14](Обрабатываем запрос, получаем AsyncMap)
@[15-25](Используем полученный AsyncMap)
@[16-24](Получаем значение по ключу)
@[17-20](Успешное получение значения)
@[20-23](Не получили значение)
@[25-30](Не получили AsyncMap)
@[35-42](Завершение запроса с возвратом значения)
@[44-48](Завершение запроса с ошибкой)

---?code=sodium-store/src/main/kotlin/com/lezhnin/project/sodium/store/reader/ReaderVerticle.kt&lang=kotlin&title=Reader Verticle

@[10-20](Инициализация конфигурации)
@[22-28](Обработчик получения начальной конфигурации)
@[22-28](Обработчик обновления начальной конфигурации)

+++?code=sodium-store/src/main/kotlin/com/lezhnin/project/sodium/store/reader/MasterConfig.kt&lang=kotlin&title=Master Config

@[12-19](Параметры и поля)
@[21-36](Основной конструктор)
@[24-30](Обработчик получения главной конфигурации)
@[31-34](Обработчик обновления главной конфигурации)
@[38-41](Метод остановки обработчиков главной конфигурации)
@[43-60](Метод перезагрузки главной конфигурации)
@[43-60](Метод перезагрузки главной конфигурации)
@[44-45](Очистка загрузчиков)
@[48-58](Инициализация загрузчика)
@[62-77](Метод конфигурирования ConfigRetriever)

+++?code=sodium-store/src/main/kotlin/com/lezhnin/project/sodium/store/reader/ReadHandler.kt&lang=kotlin&title=Read Handler

@[9-20](Параметры, поля и обработка данных)
@[21-36](Запись данных в AsyncMap)

+++?code=sodium-store/src/main/kotlin/com/lezhnin/project/sodium/store/reader/ChangeHandler.kt&lang=kotlin&title=Change Handler

---?image=assets/bg/lazy-cats-leaves-animals-opt.jpg
## @fa[spinner fa-pulse fa-fw] Живая демострация котиков

---?image=assets/bg/lazy-cats-leaves-animals-opt.jpg
## @fa[check-square-o] Конец презентации
### @fa[thumbs-o-up] Спасибо за внимание!
