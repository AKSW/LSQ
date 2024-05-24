Helper docker file to build the image from an uberjar and thus avoid a full build.

For example, this git repository might be checked out on a remote server with only docker available.
In this case, the lsq jar could be built locally and copied to the remote server using `scp`.

The filename must match the pattern: `lsq-pkg-uberjar-cli-*-jar-with-dependencies.jar`
This is also the file generated when running `make distjar` on the project root.

Once the uberjar is present, run `docker build .` to build the image.

