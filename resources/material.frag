
varying vec3 v_normal;

void main() {
	vec3 normal = v_normal;
	//vec4 diffuse = vec4(1.0);

	//gl_FragColor.rgb = diffuse.rgb;
	//gl_FragColor.a = 1.0;
	gl_FragColor = vec4(normal, 1.0);
}
