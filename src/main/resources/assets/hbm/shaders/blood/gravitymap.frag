#version 120

uniform vec3 gravity;

varying vec2 texCoord;
varying vec3 normal;
varying vec3 tangent;

uniform sampler2D mainTex;

void main(){
	ivec2 fragPos = ivec2(gl_FragCoord.xy);
	vec3 bitangent = normalize(cross(normal, tangent));
	mat3 tbn = transpose(mat3(tangent, bitangent, normal));
	
	vec3 testGravity = tbn * gravity;
	
	vec2 grav = testGravity.xy;
	//vec2 grav = vec2(dot(tangent, gravity), dot(bitangent, gravity));
	//float tex = texelFetch(texture, fragPos, 0).a;
	float tex = texture2D(mainTex, texCoord).b;
	gl_FragColor = vec4(grav*0.5 + 0.5, tex, 1);
}
