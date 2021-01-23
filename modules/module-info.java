module rtaware.jigsaw {
	requires transitive java.desktop;
	requires jdk.jfr;
	requires jdk.management;
	requires jdk.management.agent;
	requires jdk.incubator.foreign;

	exports com.rtaware.jigsaw;
	exports com.rtaware.jigsaw.event;
}