<p align="left">
  <img width="20%" height="20%" src="https://i.TODO.png">
  <br> <br>
  <a href="https://www.codacy.com/gh/NivixX/NDatabase/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=NDatabase/NivixX&amp;utm_campaign=Badge_Grade"><img src="https://app.codacy.com/project/badge/Grade/521e578f30d64d7d9e4d4eb30057c086"/></a>
  <a><img alt="Issues" src="https://img.shields.io/github/issues/NivixX/NDatabase"></a>
  <a><img alt="Forks" src="https://img.shields.io/github/forks/NivixX/NDatabase"></a>
  <a><img alt="Stars" src="https://img.shields.io/github/stars/NivixX/NDatabase"></a>
  <a><img alt="License" src="https://img.shields.io/github/license/NivixX/NDatabase"></a>
  <a><img alt="Authors" src="https://img.shields.io/badge/Authors-NivixX-blue"></a>  
</p>

# NDatabase (WIP)
NDatabase is a lightweight and easy to use
[key-value store](https://en.wikipedia.org/wiki/Key%E2%80%93value_database) style database framework mainly aimed for minecraft servers and is multi-platform (currently supporting Bukkit / Spigot servers).
It can be used as a plugin so you can install it once and use it everywhere without having to configure a database and duplicate connection pool each time you develop a new plugin. The API provide a fluent way to handle Async data fetch and server
main thread callback with [async to sync](#async-to-sync) mechanism. NDatabase can support multiple databases (currently MySQL and In-memory implementation).


## How does it work & API usage
If you want to use NDatabase for your minecraft server, you can install it as a plugin easily, see [installation & quickstart](#async-to-sync).
When NDatabase is running on your server, Creating the schema and repository for your data model is very easy, you can actually do that with one line of code.


```java
Repository<UUID, PlayerStats> repository = NDatabase.api().getOrCreateRepository(PlayerStats.class);
```
Your repository is now ready, and you can now use it for operations such as insert, update, delete, get, find, etc. Note that you can recall the same method to get your repository from anywhere as the repository instance for this class type is cached.

Here is an overview about how it works and how can it be used with multiple plugins.

<img src="https://i.imgur.com/K6Q1lBo.jpg" alt="drawing" height="240"/>

### Fluent async to sync API
As you may know, a minecraft server has a __*main thread*__ which handle the logic game tick and synchronisation, the server can tick up to 20 times per seconds (50 ms per tick) which mean, if you are doing heavy process in the main thread and it lead the server to take more than 50 ms to tick, your server will __*lag*__ and tick less often.

That's why you should always process heavy task and I/O task __*asynchronously*__, but there is another issue, you can't/should not mute the game state (eg: call Bukkit methods) asynchronously because it will __*break the synchronization*__ or even crash your server. Most of server software will simply prevent you from doing that.

In the scenario where you want to retrieve data asynchronously and use it inside your game context, you can do that by using the bukkit __*scheduler*__. The idea is to get the data in another thread and then schedule in the main thread, a task that is consuming your retrived data. It's doable by using the Bukkit methods but NDatabase provide you a fluent API to do that.

Here is two examples:
```java
Repository<String, BlockDTO> blockRepository = NDatabase.api().getOrCreateRepository(BlockDTO.class);
// Async to Sync (get data async and consume it in the main thread)
blockRepository.getAsync("id")
        .thenSync((bloc, exception) -> {
            if(exception != null) {
                // Handle exception
                return;
            }
            placeBlockInWorld(bloc);
        });

Repository<UUID, PlayerDTO> playerRepository = NDatabase.api().getOrCreateRepository(PlayerDTO.class);
// Full Async (get data async and consume it in the same async thread)
playerRepository.getAsync(joinedPlayer.getUUID())
        .thenAsync((playerDTO, exception) -> {
            if(exception != null) {
                // Handle exception
                return;
            }
            loadPlayer(playerDTO);
        });
```
<img src="https://i.imgur.com/q43cdhp.jpg" alt="drawing"/>

* **Async to sync**: in the first example we retrieve the data of a block asynchronously, as we know we should not change the game state asynchronously, we give a consumer callback that will be scheduled and run in the main thread. This approach __*doesn't affect main thread's performances*__ as we retrieve the data in another thread.

* **Full async**: in the second example, we retrieve the data of a player who just connected to the server asynchronously and consume this data in the same async thread, because we don't necessarily have to do bukkit operation but just cache some informations, so all this can be done of the main thread. Keep in mind that you should use concurrent collections to avoid getting `ConcurrentModificationException`


## Installation & QuickStart usage
### Run the NDatabase plugin (Bukkit / Spigot)
1. Put the NDatabase plugin in your `/plugins` folder. You can build the jar yourself or download the latest release HERE
2. (Optional) start your server and edit your `/plugins/NDatabase/config.yml` as your needs and what database your are using. By default, the configuration is set to a `IN_MEMORY` implementation, so this is very useful if you want to develop your plugin without deploying a database.
<details>
  <summary>Show config file</summary>

```yaml
# Your database type
# - IN_MEMORY (default)
# - MYSQL
database-type: IN_MEMORY

database:
  mysql:
    host: 'localhost'
    port: 3306
    user: ''
    pass: ''

    # Your MySQL database, note that you have to create the database yourself
    # and give the permission to your configured user
    database-name: ''

    # Depending on your MySQL version or fork, you may want to change the driver
    # default com.mysql.jdbc.Driver
    driver-class-name: 'com.mysql.jdbc.Driver'

    # Set the minimum constantly opened connection with your database
    # note that the number of plugin won't multiply this number
    # so you can config this number according to your needs
    minimum-idle-connection: 3

    # Maximum number of connection in the pool
    maximum-pool-size: 10
```
</details>

Once the NDatabase plugin is running on your server, you are good to go and you can use the API in any of your plugins without reconfiguring a database.

### Add dependency to your project
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
dependencies {
    implementation 'com.github.User:Repo:Tag' TODO
}
```

### Define your data model

First let's define our data model, you just have to create a class which extends `NEntity\<K>` where K is the type of your key, currently UUID, String, Long, Integer types are supported.
```java
@NTable(name = "player_statistics", schema = "", catalog = "")
public class PlayerStatsDTO extends NEntity<UUID> {

    @JsonProperty("killCount")
    private int kills;

    @JsonProperty("deathCount")
    private int deaths;

    @JsonProperty("lastLocation")
    private SerializedLocation lastLocation;

    // ... with getters / setters

    // Note that you need a default constructor as the Framework will use it
    public PlayerStatsDTO() { }
}
```
Define your table name with the `@NTable` annotation, only the `name` is mandatory, you can choose to not specify a `schema` or `catalog`.

Behind the scene, NDatabase will convert your object to a JSON, it's optional, but I recommend specifying the field name with the `@JsonProperty` annotation, because your schema will still work even if you change your variable name.

Last but not least, you can embed other objects inside your data model. 
Every object **must** be `Serializable`, so for instance, you cannot store a `Bukkit.Location` object as it is linked to your minecraft server domains context, which is volatile. 
I recommend you to check the [Data model design best practices](#async-to-sync) section to know best practices and *domain<>dto* separation.
### Repository API usage
There is all operations you can do with a repository,  mostly all of them implements a *Sync* and an *Async* version.

| repository.\<method-name> | Sync | Async | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|---------------------------|------|-------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| get(*key*)                | ✔️   | ✔️    | Return an entity by key, return null if doesn't exist                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| insert(*nEntity*)         | ✔️   | ✔️    | Insert an entity, will throw an `DuplicateKeyException` if a value with this key already exist.                                                                                                                                                                                                                                                                                                                                                                                                   |
| upsert(*nEntity*)         | ✔️   | ✔️    | Insert or update an entity if already exist, most of database support upsert operation without extra performances cost, so you can use it over `insert` if you don't want to worry about if the value is already present or not.                                                                                                                                                                                                                                                                  |
| update(*nEntity*)         | ✔️   | ✔️    | Update an existing entity, will throw an `NEntityNotFoundException` if a value with this key doesn't exist                                                                                                                                                                                                                                                                                                                                                                                        |
| delete(*nEntity*)         | ✔️   | ✔️    | Delete an existing entity, will throw an `NEntityNotFoundException` if an entity with this key doesn't exist                                                                                                                                                                                                                                                                                                                                                                                      |
| delete(*key*)             | ✔️   | ✔️    | Same as previous but only with key argument                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| streamAllValues(*key*)    | ✔️   | ✔️    | Fetch all your entities into a `Stream`, note that it will not overflow your memory as the stream internally work with a cursor, more info about how a cursor work [here](https://docs.oracle.com/javase/tutorial/jdbc/basics/retrieving.html)                                                                                                                                                                                                                                                    |
| find(*predicate*)         | ❌    | ✔️    | Find a list of entities given a predicate (for example `playerDTO -> playerDTO.getScore() > 20`). /!\ be aware that by design, the Key-value store value's fields are not indexed, this method will stream your entities, convert it into objects and apply your predicate, so you don't wanna overuse it especially for large databases. This process could be highly improved in the future by converting the lambda predicate expression into a query, see [future objectives](#async-to-sync) |
| findOne(*predicate*)      | ❌    | ✔️    | Same as above but only return one result that match your predicate                                                                                                                                                                                                                                                                                                                                                                                                                                |
| deleteAll()               | ✔️   | ✔️    | Truncate your collection                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |

### Exception handling
All throwed exception will be wrapped as `NDatabaseException`and are runtime exceptions. You can catch more specific exception if it's required (see here). The way exceptions are handled will be different between sync and async.

If you want to handle an exception **__asynchronously__**, you have two ways to consume the exception if the async task ended exceptionally
```
repository.getAsync(player.getUUID())
        .thenSync((playerDTO, throwable) -> {
            if(throwable != null) {
                // an exception occurred in the async thread
                // handle your exception here
                player.kickPlayer("an error occurred while loading your profile, please try again later.");
                return;
            }
            loadPlayerInventory(player, playerDTO);
        });
```
If handling the exception is not critical or required, you can choose to ignore it by using the monoConsumer lambda version. NDatabase will catch the exception in the async thread for you and print the error message. Note that your callback won't be called if there is an exception.
```
repository.getAsync(player.getUUID())
        .thenSync((playerDTO) -> {
            // If there is an exception, this callback will not be called
            loadPlayerInventory(player, playerDTO);
        });
```

## Build fat jar or API
`mvn clean install -DSkipTests`

It will create the complete jar in `ndatabase-packaging-jar/target` and the API in `ndatabase-api/target`

### Data model design best practices
WIP
### Future objectives
WIP
#### Built in mappers & automapped object wrapper
WIP