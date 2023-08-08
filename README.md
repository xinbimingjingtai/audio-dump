# Audio dump

cloud/qq music dump


## Environment & Tool

- JRE 8
- Build tool Maven
- Dev tool IDEA


## Usage

`java [options] -jar <dump-executable.jar> <directory|absolute-path>`


### Options

```text
-DdumpMode=ASYNC
	The dump is sync by default, only 'ASYNC' means that the dump use async
-Dremark=OFF
	The remark is enabled by default, only 'OFF' means that the remark is disabled
-DlogFileToggle=OFF
	The log is enabled by default, only 'OFF' means that the log is disabled
-DlogFileDirectory=<log-file-directory>
	Specify the log output directory
-DlogFileName=<log-filename>
	Specify the log output filename
```

Note: If param(the <xxx> part) contains whitespace character, use quotes surround with it. You can run `mvn package`,
then the `<executable>.jar` will be generated (filename end with `shaded.jar`) in directory `target`.


## Supported formats

- cloud music: ncm
- qq music: qmc3, qmc0, qmcflac

## References

- [ncmdump](https://github.com/qaralotte/ncmdump)
- [qmcflactomp3](https://github.com/OnlyPiglet/qmcflactomp3)
