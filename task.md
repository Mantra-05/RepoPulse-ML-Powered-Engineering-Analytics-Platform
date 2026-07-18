# RepoPulse Phases 3–13 Task List

## Phase 3 – JPA Entities & Database
- [x] Modify `Prediction.java` (OneToOne, add risk/priority/health fields)
- [x] Modify `PullRequest.java` (add OneToOne back-reference to Prediction)
- [x] Create `RepositoryAnalysis.java` entity
- [x] Create `RepositoryAnalysisRepository.java`
- [x] Enhance `PullRequestRepository.java`, `CommitRepository.java`, `ContributorRepository.java`
- [x] Create `RepositoryAnalysisResponse.java` DTO
- [x] Create `RepositoryAnalysisService.java` + `RepositoryAnalysisServiceImpl.java`
- [x] Create `RepositoryAnalysisController.java`

## Phase 4 – GitHub Integration
- [x] Update `application.yml` (add github config)
- [x] Create `GitHubConfig.java` (RestTemplate bean)
- [x] Create GitHub DTO package (8 DTOs)
- [x] Create `GitHubService.java` interface
- [x] Create `GitHubServiceImpl.java` (paginated API calls)
- [x] Update `RepositoryServiceImpl.java` to wire GitHubService

## Phase 5 – Repository Analyzer (covered with Phase 3 entities)
- [x] Implement Repository Analysis endpoints and service logic

## Phase 6 – ML Dataset (Python)
- [x] Create `ml/requirements.txt`
- [x] Create `ml/data/raw_dataset.csv` (500-row sample)
- [x] Create `ml/preprocess.py`

## Phase 7 – ML Model
- [x] Create `ml/train.py`

## Phase 8 – Prediction FastAPI
- [x] Create `ml/main.py` (FastAPI app)
- [x] Create `ml/schemas.py` (Pydantic models)

## Phase 9 – Backend → ML Bridge
- [x] Update `PredictionRequest.java` DTO
- [x] Update `PredictionResponse.java` DTO
- [x] Create `MlPredictionRequest.java` / `MlPredictionResponse.java`
- [x] Update `PredictionService.java` + `PredictionServiceImpl.java`
- [x] Update `PredictionController.java`
- [x] Add ML service URL to `application.yml`

## Phases 10–13 – Frontend (React + Vite + TailwindCSS)
- [x] Initialize Vite project (`frontend/`)
- [x] Configure TailwindCSS, theme tokens, global styles
- [x] Create Axios client with JWT interceptors
- [x] Create API service layer (authApi, repositoryApi, predictionApi)
- [x] Create `AuthStore` / `useAuth` hook
- [x] Create `Sidebar` component (responsive, dark)
- [x] Create `LoginPage`
- [x] Create `DashboardPage` (stat cards)
- [x] Create `RepositoryListPage` + `RepositoryDetailPage`
- [x] Create `PullRequestsPage` (table, filters, pagination)
- [x] Create `AnalyticsPage` (Chart.js charts)
- [x] Create `PredictionPage`
- [x] Create Chart components (Gauge, Pie, Bar, Line) (incorporated in AnalyticsPage)
- [x] Create `SkeletonLoader`, `Toast`, `Spinner`, `ErrorPage`, `NotFoundPage`
- [x] Add search / filter / pagination to repository and PR pages
- [x] Finish App routing (App.tsx / main.tsx) and Layout
