# This group defines a "default" target.
# Running `docker compose bake` without arguments will build all targets in this group in parallel.
group "default" {
  targets = [
    "books-service"
  ]
}

variable "IMAGE_NAME" {
  default = "books-service"
}

# A variable for the default version tag, making it easy to update.
variable "VERSION" {
  default = "1.0"
}

# Build definition for the Books Service.
target "books-service" {
  context = "."
  tags    = ["${IMAGE_NAME}:${VERSION}"]
  args = {
    PROJECT_NAME    = "books-service"
    PROJECT_VERSION = "${VERSION}"
  }
}