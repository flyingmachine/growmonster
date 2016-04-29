# growmonster

growmonster is a tiny library that I originally used to write and load
database fixtures. Its purpose is to allow you to compactly represent
entities and to allow entity attributes to reference other
entities. Quick example:

```clojure
(inflatev
 [:mages
  [:xanax
   {:name "xanax" :id 1}

   :vicodin
   {:name "vicodin" :id 2}]

  :spells
  [[{:author [:mages :xanax :id]}
    {:name "drowsiness" :id 10}
    {:name "dizziness" :id 11}]

   {:name "paranoia"
    :author [:mages :vicodin :id]
    :id 20}]])

; =>
[{:name "xanax", :id 1}
 {:name "vicodin", :id 2}
 {:author 1, :name "drowsiness", :id 10}
 {:author 1, :name "dizziness", :id 11}
 {:name "paranoia", :author 2, :id 20}]
```

The above example might leave you asking, "Isn't the fixture actually
more verbose than its output?" You'll have to just trust me that using
fixtures ends up being more compact, plus it makes it easier to manage
all those references.

Also consider a datomic fixture:

```clojure
[:users
 [:billy
  {:db/id #db/id [:db.part/user]
   :user/username "billy"}]

 :posts
 [{:db/id #db/id [:db.part/user]
   :content/owner [:users :billy :db/id]
   :content/body  "some post content"}]]
```

In this example, the value of `:db/id` can't be hard-coded because it
can't be known before the fixture is read (`#db/id [:db.part/user]` is
a reader macro which returns a value once the expression is read).

## Writing Fixtures

Fixture have a hierarchical structure. At the top are keyword/vector
pairs. The keyword is a meaningful name for a group of values, and the
vector is the group of values. Here's a very simple example:

```clojure
(inflatev
 [:users
  [{:user/username "billy"}]

  :posts
  [{:content/body "some post content"}]])
; =>
[{:user/username "billy"} {:content/body "some post content"}]
```

In this example, `:users` and `:posts` are the group names. Each group
vector has only one entity in it.

### Referencing Entities

If you want to reference an entity, first you have to name the
entity. To name an entity, just place a keyword before the entity:

```clojure
[:users
 [:billy
  {:user/username "billy"}]]
```

In this case, the user entity's name is `:billy`. To reference this
entity, use a vector as an entity value:

```clojure
(inflatev
 [:users
  [:billy
   {:user/username "billy"}]

  :posts
  [{:content/owner [:users :billy :user/username]
    :content/body  "some post content"}]])
; =>
[{:user/username "billy"}
 {:content/owner "billy", :content/body "some post content"}]
```

In this example, there's a post whose `:content/owner` is the
`:user/username` for `:billy`. It's as if there's a map

```clojure
{:users {:billy {:user/username "billy}}}
```

and the vector `[:users :billy :user/username]` in the means

```clojure
(get-in {:users {:billy {:user/username "billy}}}
        [:users :billy :user/username]
```

### Grouping Common Attributes

Often when you're writing fixtures you'll want to associate many
entities with the same parent - like many posts with the same
author. To do that, you wrap those entities in a vector, where the
first element of the vector is a map containing the common attributes:


```clojure
(inflatev
 [:users
  [:billy
   {:user/username "billy"}]

  :posts
  [[{:content/owner [:users :billy :user/username]}
    {:content/body "some post content"}
    {:content/body "another post"}
    {:content/body "third post"}]]])
; =>
[{:user/username "billy"}
{:content/owner "billy", :content/body "some post content"}
{:content/owner "billy", :content/body "another post"}
{:content/owner "billy", :content/body "third post"}]
```

You can have multiple sets entities with different common
attributes. For example, if your fixture has two users with multiple
posts:

```clojure
(inflatev
 [:users
  [:billy
   {:user/username "billy"}

   :timmy
   {:user/username "timmy"}]

  :posts
  [[{:content/owner [:users :billy :user/username]}
    {:content/body "some post content"}
    {:content/body "another post"}
    {:content/body "third post"}]

   [{:content/owner [:users :timmy :user/username]}
    {:content/body "beast mode timmy"}
    {:content/body "get wrecked"}
    {:content/body "where is this coming from"}]
    
   {:content/body "I have no owner"}]])
; =>
[{:user/username "billy"}
 {:user/username "timmy"}
 {:content/owner "billy", :content/body "some post content"}
 {:content/owner "billy", :content/body "another post"}
 {:content/owner "billy", :content/body "third post"}
 {:content/owner "timmy", :content/body "beast mode timmy"}
 {:content/owner "timmy", :content/body "get wrecked"}
 {:content/owner "timmy", :content/body "where is this coming from"}
 {:content/body "I have no owner"}]
```

Notice that you also mixed entities with common attributes (those
owned by timmy or billy) with entities that don't have common
attributes (the last post, `{:content/body "I have no owner"}`).
