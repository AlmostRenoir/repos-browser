# repos-browser

Simple service to browse Git repositories. Current implementation applies to GitHub.

## Technologies

- Application is written with **Java 17**, **Maven** and **Spring Boot**. 
- The solution is based on a reactive approach with **WebFlux**.
- For testing, a standard set of **JUnit**, **Mockito**, **WireMock** and **WebTestClient** is used.

## Running the application locally

The easiest way is to execute the `main` method in the `almostrenoir.reposbrowser.ReposbrowserApplication` class from your IDE.

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html):

```shell
mvn spring-boot:run
```

## API

#### **GET** */git-repos/by-user/{username}/no-forks*

**Response:**

*application/json*

```json
[{
  "name": "string",
  "owner": "string",
  "branches": [
    {
      "name": "string",
      "lastCommitSHA": "string"
    }
  ]
}]
```

**Statuses:**

- 200 - OK
- 400 - Invalid username
- 404 - User with given username not exist
- 406 - Not acceptable content type
- 500 - Problem with GitHub connection or internal errors

**Notes:**

In contrast to the direct GitHub API, this endpoint enables fetching all branches without pagination. 
While this is advantageous, it could lead to situations where users with numerous repositories might face retrieval issues due to exceeding timeouts. 
In the current implementation, you have the option to modify this manually within the `almostrenoir.reposbrowser.gitrepos.services.usersrepos.github.GithubUsersReposService`.

## License

[MIT](https://choosealicense.com/licenses/mit/)