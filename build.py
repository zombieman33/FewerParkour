"""Compile from cached provided APIs; alternatively use the Maven pom normally."""
from pathlib import Path
import os,subprocess,zipfile
root=Path(__file__).resolve().parent
jdk=Path(os.environ.get('FEWER_JAVA_HOME','C:/Program Files/Java/jdk-25.0.2'))
workspace=root.parent.parent
repository=Path.home()/'.m2/repository'
if os.environ.get('PARKOUR_BUILD_CLASSPATH'):
 jars=[Path(p) for p in os.environ['PARKOUR_BUILD_CLASSPATH'].split(os.pathsep)]
else:
 paper=workspace/'.work/paper-26.3-upgrade/libraries/io/papermc/paper/paper-api/26.3.build.159-beta'
 jars=list(paper.glob('*.jar'))+[p for p in repository.rglob('*.jar') if all(s not in str(p) for s in ['paper-api','spigot-api','bukkit'])]
 if not list(paper.glob('*.jar')):raise SystemExit('Provide PARKOUR_BUILD_CLASSPATH or use Maven with the provided API dependencies.')
out=root/'target/classes';out.mkdir(parents=True,exist_ok=True)
for old in out.rglob('*.class'):old.unlink()
a=['-proc:none','--release','21','-encoding','UTF-8','-cp',os.pathsep.join(map(str,jars)),'-d',str(out)]+list(map(str,(root/'src/main/java').rglob('*.java')))
f=root/'target/javac.args';f.write_text('\n'.join('"'+v.replace('\\','/')+'"' for v in a),encoding='utf-8');subprocess.run([str(jdk/'bin/javac.exe' if os.name=='nt' else jdk/'bin/javac'),'@'+str(f)],check=True)
jar=root/'target/FewerParkour-1.4.2-fewer.1.jar'
with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as z:
 for p in out.rglob('*.class'):z.write(p,p.relative_to(out).as_posix())
 for p in (root/'src/main/resources').rglob('*'):
  if p.is_file():z.writestr(p.relative_to(root/'src/main/resources').as_posix(),p.read_bytes().replace(b'${project.version}',b'1.4.2-fewer.1'))
print(jar)
