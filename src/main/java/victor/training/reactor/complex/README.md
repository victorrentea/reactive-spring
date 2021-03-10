

- Start with a List<productId>
- Call in a loop externalApi.getProductData(productId):Product using RestTemplate; setup wiremock with delay
- Get back a Flux<Product> (start with Flux.fromIterable(ids))
- Fetch the data from a function returning Mono.just(result)
- Run the blocking REST call in the appropriate Scheduler (which one?)
- Move to nonBlocking REST calls with WebClient

- Play: what if you have 10k products to fetch ?
- Call their API with max 10 requests in parallel <<<
- Avoid calling network in a loop by fetching "pages of Products" 
  ExternalApi.getProductData(List<productId>): List<Product>
  
- Products can be resealed. If they are, an audit API REST call should happen passing the id of the product.