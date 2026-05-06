import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    environment: "node",
    globals: true,
    include: ["src/**/*.test.ts", "src/**/*.test.js"],
    reporters: ["verbose"],
    coverage: {
      provider: "v8",
      reportOnFailure: true,
      reporter: ["text", "text-summary", "html", "lcov"],
      reportsDirectory: "./coverage",
      all: true,
      include: [
        "src/utils/schemaValidator.js",
        "src/utils/diagramUtils.js",
        "src/utils/sqlGenerator.js",
        "src/utils/canvasReducer.js",
        "src/utils/llmParser.js",
      ],
      exclude: ["src/**/*.test.ts", "src/**/*.test.js"],
      thresholds: {
        lines: 80,
        functions: 80,
        branches: 80,
        statements: 80,
      },
    },
  },
});
