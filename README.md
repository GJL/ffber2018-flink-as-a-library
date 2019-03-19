### Sources
	https://github.com/GJL/flink/tree/rescale
	https://github.com/tillrohrmann/flink/tree/nativeKubernetes

### Starting minikube

	minikube ssh 'sudo ip link set docker0 promisc on'
	minikube start --memory 4096

	kubectl create rolebinding default-cluster-admin --clusterrole=cluster-admin --serviceaccount=default:default  --namespace=default

### Flink UI k8s
	
	http://192.168.99.100:30081/#/overview

### opaque container mode

#### using envsubst

	FLINK_IMAGE_NAME=flink:rescaling FLINK_JOB=com.dataartisans.StreamingJob FLINK_JOB_PARALLELISM=1 envsubst < job-cluster-job.yaml.template | kubectl create -f -
	kubectl create -f job-cluster-service.yaml
	FLINK_IMAGE_NAME=flink:rescaling FLINK_JOB_PARALLELISM=1 envsubst < task-manager-deployment.yaml.template | kubectl create -f -

#### prepared resource cfgs

	kubectl create -f job-cluster-job.yaml
	kubectl create -f job-cluster-service.yaml
	kubectl create -f task-manager-deployment.yaml

#### scale TMs
	kubectl scale deployment flink-task-manager --replicas=3	

#### clean up
	kubectl delete job flink-job-cluster && kubectl delete service flink-job-cluster

#### clean up opaque container mode
	kubectl delete deployment flink-task-manager


### session mode k8s

	kubectl create -f jobmanager-deployment.yaml
	kubectl create -f jobmanager-service.yaml

#### submit job
	
	bin/flink run -d -m 192.168.99.100:30081 [...]/ffber18/target/ffber18-1.0-SNAPSHOT.jar
	bin/flink modify -p 4 -m 192.168.99.100:30081

#### clean up session
	kubectl delete service flink-jobmanager && kubectl delete deployment flink-jobmanager


### Build Docker Image

	eval $(minikube docker-env)

#### Build opaque container mode image
	git checkout rescale

	./build.sh --from-archive [...]/flink-ff472b46c192d174d3e652cb6a704e0172842272.tgz  --job-jar [...]/ffber18/target/ffber18-1.0-SNAPSHOT.jar  --image-name flink:rescaling

#### Build session cluster image
	git checkout nativeKubernetes

	/build.sh --from-archive [...]/flink-kubernetes.tgz --image-name flink:kubernetes

### clean up kubernetes
	kubectl delete deployment flink-task-manager
	kubectl delete service flink-job-cluster
	kubectl delete job flink-job-cluster